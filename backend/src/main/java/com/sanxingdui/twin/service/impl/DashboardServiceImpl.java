package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.entity.Artifact;
import com.sanxingdui.twin.entity.GuideRoute;
import com.sanxingdui.twin.entity.Literature;
import com.sanxingdui.twin.entity.Model3d;
import com.sanxingdui.twin.entity.QaMessage;
import com.sanxingdui.twin.entity.QaSession;
import com.sanxingdui.twin.entity.Reservation;
import com.sanxingdui.twin.entity.Review;
import com.sanxingdui.twin.entity.SysUser;
import com.sanxingdui.twin.mapper.ArtifactMapper;
import com.sanxingdui.twin.mapper.DashboardMapper;
import com.sanxingdui.twin.mapper.ExcavationPitMapper;
import com.sanxingdui.twin.mapper.GuideRouteMapper;
import com.sanxingdui.twin.mapper.LiteratureMapper;
import com.sanxingdui.twin.mapper.Model3dMapper;
import com.sanxingdui.twin.mapper.QaMessageMapper;
import com.sanxingdui.twin.mapper.QaSessionMapper;
import com.sanxingdui.twin.mapper.ReservationMapper;
import com.sanxingdui.twin.mapper.ReviewMapper;
import com.sanxingdui.twin.mapper.SysUserMapper;
import com.sanxingdui.twin.service.ArtifactService;
import com.sanxingdui.twin.service.DashboardService;
import com.sanxingdui.twin.service.KnowledgeGraphService;
import com.sanxingdui.twin.vo.DashboardOverviewVO;
import com.sanxingdui.twin.vo.NameValueVO;
import com.sanxingdui.twin.vo.TrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 数据大屏统计服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;
    private final ArtifactMapper artifactMapper;
    private final ExcavationPitMapper pitMapper;
    private final Model3dMapper model3dMapper;
    private final LiteratureMapper literatureMapper;
    private final SysUserMapper userMapper;
    private final QaSessionMapper qaSessionMapper;
    private final QaMessageMapper qaMessageMapper;
    private final GuideRouteMapper routeMapper;
    private final ReservationMapper reservationMapper;
    private final ReviewMapper reviewMapper;
    private final KnowledgeGraphService kgService;
    private final ArtifactService artifactService;

    @Override
    public DashboardOverviewVO overview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setArtifactCount(artifactMapper.selectCount(null));
        vo.setTreasureCount(artifactMapper.selectCount(new LambdaQueryWrapper<Artifact>().eq(Artifact::getIsTreasure, 1)));
        vo.setPitCount(pitMapper.selectCount(null));
        vo.setModelCount(model3dMapper.selectCount(new LambdaQueryWrapper<Model3d>().eq(Model3d::getStatus, 1)));
        List<Map<String, Object>> g = model3dMapper.selectMaps(new QueryWrapper<Model3d>()
                .select("IFNULL(SUM(gaussian_count), 0) AS total").eq("status", 1));
        vo.setGaussianTotal(g.isEmpty() || g.get(0) == null ? 0L : ((Number) g.get(0).get("total")).longValue());
        vo.setEntityCount((long) kgService.snapshot().getEntities().size());
        vo.setRelationCount((long) kgService.snapshot().getRelations().size());
        vo.setLiteratureCount(literatureMapper.selectCount(null));
        vo.setUserCount(userMapper.selectCount(null));
        vo.setQaSessionCount(qaSessionMapper.selectCount(null));
        vo.setQaQuestionCount(qaMessageMapper.selectCount(new LambdaQueryWrapper<QaMessage>().eq(QaMessage::getRole, "user")));
        vo.setRouteCount(routeMapper.selectCount(new LambdaQueryWrapper<GuideRoute>().eq(GuideRoute::getStatus, 1)));
        vo.setReservationCount(reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .in(Reservation::getStatus, "PENDING", "CONFIRMED", "COMPLETED")));
        List<Map<String, Object>> today = reservationMapper.selectMaps(new QueryWrapper<Reservation>()
                .select("IFNULL(SUM(visitor_count), 0) AS total").eq("visit_date", LocalDate.now())
                .in("status", "PENDING", "CONFIRMED", "COMPLETED"));
        vo.setTodayReservationVisitors(today.isEmpty() || today.get(0) == null ? 0L : ((Number) today.get(0).get("total")).longValue());
        vo.setVisitors30d(dashboardMapper.visitTrend(30).stream()
                .mapToLong(m -> ((Number) m.get("offlineVisitors")).longValue()).sum());
        List<Map<String, Object>> avg = reviewMapper.selectMaps(new QueryWrapper<Review>()
                .select("IFNULL(AVG(rating), 0) AS avgRating").eq("status", 1));
        double avgRating = avg.isEmpty() || avg.get(0) == null ? 0 : ((Number) avg.get(0).get("avgRating")).doubleValue();
        vo.setAvgRating(Math.round(avgRating * 10) / 10.0);
        return vo;
    }

    @Override
    public List<NameValueVO> artifactDistribution(String dimension) {
        Map<String, Supplier<List<NameValueVO>>> handlers = Map.of(
                "category", dashboardMapper::countByCategory,
                "topCategory", dashboardMapper::countByTopCategory,
                "pit", dashboardMapper::countByPit,
                "material", dashboardMapper::countByMaterial,
                "period", dashboardMapper::countByPeriod,
                "hall", dashboardMapper::countByHall);
        Supplier<List<NameValueVO>> handler = handlers.get(dimension);
        if (handler == null) {
            throw new BusinessException("不支持的统计维度：" + dimension + "，可选 category/topCategory/pit/material/period/hall");
        }
        return handler.get();
    }

    @Override
    public TrendVO visitTrend(int days) {
        int d = Math.min(Math.max(days, 1), 365);
        List<Map<String, Object>> rows = dashboardMapper.visitTrend(d);
        TrendVO vo = new TrendVO();
        vo.setDates(rows.stream().map(r -> String.valueOf(r.get("statDate"))).toList());
        Map<String, String> seriesNames = new LinkedHashMap<>();
        seriesNames.put("offlineVisitors", "线下客流");
        seriesNames.put("onlineVisits", "线上访问");
        seriesNames.put("modelViews", "3D模型浏览");
        seriesNames.put("qaCount", "智能问答");
        seriesNames.put("reservationCount", "预约单数");
        seriesNames.forEach((key, label) -> vo.getSeries().put(label,
                rows.stream().map(r -> r.get(key) == null ? 0L : ((Number) r.get(key)).longValue()).toList()));
        return vo;
    }

    @Override
    public Map<String, Object> qaStatistics(int days) {
        int d = Math.min(Math.max(days, 1), 90);
        // 补齐没有提问的日期，保证折线连续
        Map<String, Long> daily = dashboardMapper.qaDaily(d).stream()
                .collect(Collectors.toMap(NameValueVO::getName, NameValueVO::getValue));
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (int i = d - 1; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            dates.add(date);
            counts.add(daily.getOrDefault(date, 0L));
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sessionCount", qaSessionMapper.selectCount(null));
        map.put("questionCount", qaMessageMapper.selectCount(new LambdaQueryWrapper<QaMessage>().eq(QaMessage::getRole, "user")));
        map.put("anonymousSessionCount", qaSessionMapper.selectCount(new LambdaQueryWrapper<QaSession>().isNull(QaSession::getUserId)));
        map.put("dates", dates);
        map.put("dailyQuestions", counts);
        map.put("modeDistribution", dashboardMapper.qaModeDistribution());
        map.put("performance", dashboardMapper.qaPerformance());
        return map;
    }

    @Override
    public Map<String, Object> modelStatistics() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("formatDistribution", dashboardMapper.countModelByFormat());
        map.put("quality", dashboardMapper.modelQuality());
        map.put("hotArtifacts", artifactService.hot(10).stream()
                .map(a -> new NameValueVO(a.getName(), a.getViewCount().longValue())).toList());
        return map;
    }

    @Override
    public Map<String, Object> serviceStatistics() {
        Map<String, Object> map = new LinkedHashMap<>();
        Map<String, String> statusLabel = Map.of("PENDING", "待确认", "CONFIRMED", "已确认", "CANCELLED", "已取消", "COMPLETED", "已核销");
        map.put("reservationStatus", dashboardMapper.reservationStatus().stream()
                .map(n -> new NameValueVO(statusLabel.getOrDefault(n.getName(), n.getName()), n.getValue())).toList());
        map.put("upcomingReservations", dashboardMapper.upcomingReservations(7));
        map.put("ratingDistribution", dashboardMapper.ratingDistribution());
        map.put("topFavorited", dashboardMapper.topFavorited(10));
        map.put("topRoutes", dashboardMapper.topRoutes(10));
        map.put("kgEntityTypes", kgService.typeStatistics());
        return map;
    }
}
