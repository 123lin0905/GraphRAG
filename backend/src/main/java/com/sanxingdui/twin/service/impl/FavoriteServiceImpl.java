package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.common.ResultCode;
import com.sanxingdui.twin.dto.FavoriteDTO;
import com.sanxingdui.twin.entity.Favorite;
import com.sanxingdui.twin.mapper.ArtifactMapper;
import com.sanxingdui.twin.mapper.FavoriteMapper;
import com.sanxingdui.twin.mapper.GuideRouteMapper;
import com.sanxingdui.twin.security.SecurityUtils;
import com.sanxingdui.twin.service.FavoriteService;
import com.sanxingdui.twin.vo.ArtifactVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

/**
 * 收藏服务实现
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final ArtifactMapper artifactMapper;
    private final GuideRouteMapper routeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(FavoriteDTO dto) {
        Long userId = SecurityUtils.requireUserId();
        String type = StringUtils.hasText(dto.getTargetType()) ? dto.getTargetType() : "ARTIFACT";
        boolean targetExists = "ARTIFACT".equals(type)
                ? artifactMapper.selectById(dto.getTargetId()) != null
                : routeMapper.selectById(dto.getTargetId()) != null;
        if (!targetExists) {
            throw new BusinessException(ResultCode.NOT_FOUND, "收藏对象不存在");
        }
        Favorite exist = getOne(new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, type).eq(Favorite::getTargetId, dto.getTargetId()), false);
        if (exist != null) {
            removeById(exist.getId());
            if ("ARTIFACT".equals(type)) {
                artifactMapper.changeFavoriteCount(dto.getTargetId(), -1);
            }
            return false;
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setTargetType(type);
        f.setTargetId(dto.getTargetId());
        save(f);
        if ("ARTIFACT".equals(type)) {
            artifactMapper.changeFavoriteCount(dto.getTargetId(), 1);
        }
        return true;
    }

    @Override
    public boolean isFavorited(String targetType, Long targetId) {
        Long userId = SecurityUtils.currentUserId();
        return userId != null && count(new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType).eq(Favorite::getTargetId, targetId)) > 0;
    }

    @Override
    public PageResult<ArtifactVO> myArtifacts(PageQuery query) {
        Long userId = SecurityUtils.requireUserId();
        Page<Favorite> page = page(query.toPage(), new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getTargetType, "ARTIFACT").orderByDesc(Favorite::getId));
        List<Long> ids = page.getRecords().stream().map(Favorite::getTargetId).toList();
        List<ArtifactVO> list = ids.isEmpty() ? List.of() : artifactMapper.selectVoByIds(ids).stream()
                .sorted(Comparator.comparingInt(a -> ids.indexOf(a.getId()))).toList();
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), list);
    }

    @Override
    public List<Favorite> myFavorites(String targetType) {
        Long userId = SecurityUtils.requireUserId();
        return list(new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId)
                .eq(StringUtils.hasText(targetType), Favorite::getTargetType, targetType).orderByDesc(Favorite::getId));
    }
}
