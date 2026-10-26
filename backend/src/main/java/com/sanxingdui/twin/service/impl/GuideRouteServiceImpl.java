package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.common.ResultCode;
import com.sanxingdui.twin.entity.ExhibitionHall;
import com.sanxingdui.twin.entity.GuideRoute;
import com.sanxingdui.twin.entity.GuideRouteNode;
import com.sanxingdui.twin.entity.Model3d;
import com.sanxingdui.twin.mapper.ExhibitionHallMapper;
import com.sanxingdui.twin.mapper.GuideRouteMapper;
import com.sanxingdui.twin.mapper.GuideRouteNodeMapper;
import com.sanxingdui.twin.mapper.Model3dMapper;
import com.sanxingdui.twin.mapper.ArtifactMapper;
import com.sanxingdui.twin.service.GuideRouteService;
import com.sanxingdui.twin.service.ReviewService;
import com.sanxingdui.twin.vo.ArtifactVO;
import com.sanxingdui.twin.vo.RouteDetailVO;
import com.sanxingdui.twin.vo.RouteNodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 导览路线服务实现
 */
@Service
@RequiredArgsConstructor
public class GuideRouteServiceImpl extends ServiceImpl<GuideRouteMapper, GuideRoute> implements GuideRouteService {

    private final GuideRouteNodeMapper nodeMapper;
    private final ArtifactMapper artifactMapper;
    private final ExhibitionHallMapper hallMapper;
    private final Model3dMapper model3dMapper;
    private final ReviewService reviewService;

    @Override
    public RouteDetailVO detail(Long id) {
        GuideRoute route = getById(id);
        if (route == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "路线不存在");
        }
        List<GuideRouteNode> nodes = nodeMapper.selectList(new LambdaQueryWrapper<GuideRouteNode>()
                .eq(GuideRouteNode::getRouteId, id).orderByAsc(GuideRouteNode::getSeq));

        // 批量加载节点关联的文物、展厅、默认模型
        List<Long> artifactIds = nodes.stream().map(GuideRouteNode::getArtifactId).filter(Objects::nonNull).distinct().toList();
        Map<Long, ArtifactVO> artifacts = artifactIds.isEmpty() ? Map.of()
                : artifactMapper.selectVoByIds(artifactIds).stream().collect(Collectors.toMap(ArtifactVO::getId, Function.identity()));
        Map<Long, String> halls = hallMapper.selectList(null).stream()
                .collect(Collectors.toMap(ExhibitionHall::getId, ExhibitionHall::getName));
        Map<Long, String> models = artifactIds.isEmpty() ? Map.of()
                : model3dMapper.selectList(new LambdaQueryWrapper<Model3d>().in(Model3d::getArtifactId, artifactIds)
                        .eq(Model3d::getIsDefault, 1).eq(Model3d::getStatus, 1))
                .stream().collect(Collectors.toMap(Model3d::getArtifactId, Model3d::getModelUrl, (a, b) -> a));

        List<RouteNodeVO> nodeVos = nodes.stream().map(n -> {
            RouteNodeVO vo = new RouteNodeVO();
            BeanUtils.copyProperties(n, vo);
            ArtifactVO a = n.getArtifactId() == null ? null : artifacts.get(n.getArtifactId());
            if (a != null) {
                vo.setArtifactCover(a.getCoverUrl());
                vo.setHallName(a.getHallName());
                vo.setModelUrl(models.get(a.getId()));
            } else if (n.getHallId() != null) {
                vo.setHallName(halls.get(n.getHallId()));
            }
            return vo;
        }).toList();

        RouteDetailVO vo = new RouteDetailVO();
        vo.setRoute(route);
        vo.setNodes(nodeVos);
        vo.setTotalStayMinutes(nodes.stream().mapToInt(n -> n.getStayMinutes() == null ? 0 : n.getStayMinutes()).sum());
        Object avg = reviewService.summary("ROUTE", id).get("avgRating");
        vo.setAvgRating(avg == null ? 0.0 : ((Number) avg).doubleValue());
        return vo;
    }

    @Override
    public List<GuideRoute> recommend(String interests, Integer minutes) {
        List<GuideRoute> routes = list(new LambdaQueryWrapper<GuideRoute>().eq(GuideRoute::getStatus, 1)
                .le(minutes != null, GuideRoute::getDurationMin, minutes));
        List<String> tags = StringUtils.hasText(interests)
                ? Arrays.stream(interests.split("[,，]")).map(String::trim).filter(StringUtils::hasText).toList()
                : List.of();
        // 简单打分：兴趣标签命中路线名称/主题/简介越多越靠前，其次按使用次数
        return routes.stream().sorted(Comparator.comparingLong((GuideRoute r) -> -score(r, tags))
                .thenComparing(r -> -r.getUseCount())).toList();
    }

    private long score(GuideRoute r, List<String> tags) {
        String text = String.join("|", Objects.toString(r.getName(), ""), Objects.toString(r.getTheme(), ""),
                Objects.toString(r.getDescription(), ""), Objects.toString(r.getSuitableCrowd(), ""));
        return tags.stream().filter(text::contains).count();
    }

    @Override
    public void start(Long id) {
        boolean ok = update(new LambdaUpdateWrapper<GuideRoute>().eq(GuideRoute::getId, id).setSql("use_count = use_count + 1"));
        if (!ok) {
            throw new BusinessException(ResultCode.NOT_FOUND, "路线不存在");
        }
    }
}
