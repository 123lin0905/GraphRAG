package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.service.DashboardService;
import com.sanxingdui.twin.vo.DashboardOverviewVO;
import com.sanxingdui.twin.vo.NameValueVO;
import com.sanxingdui.twin.vo.TrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据大屏统计接口（返回结构适配 ECharts）
 */
@Tag(name = "数据大屏", description = "总览指标、文物分布、客流趋势、问答统计、3DGS模型统计")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "总览指标卡片")
    @GetMapping("/overview")
    public Result<DashboardOverviewVO> overview() {
        return Result.success(dashboardService.overview());
    }

    @Operation(summary = "文物分布统计", description = "dimension：category 细分类 / topCategory 一级分类 / pit 祭祀坑 / material 材质 / period 年代 / hall 展厅")
    @GetMapping("/artifact-distribution")
    public Result<List<NameValueVO>> artifactDistribution(
            @Parameter(description = "统计维度") @RequestParam(defaultValue = "topCategory") String dimension) {
        return Result.success(dashboardService.artifactDistribution(dimension));
    }

    @Operation(summary = "一次性获取全部文物分布（分类/坑/材质/年代/展厅）")
    @GetMapping("/artifact-distribution/all")
    public Result<Map<String, List<NameValueVO>>> allDistribution() {
        return Result.success(Map.of(
                "category", dashboardService.artifactDistribution("topCategory"),
                "pit", dashboardService.artifactDistribution("pit"),
                "material", dashboardService.artifactDistribution("material"),
                "period", dashboardService.artifactDistribution("period"),
                "hall", dashboardService.artifactDistribution("hall")));
    }

    @Operation(summary = "客流与访问趋势（近 N 天）")
    @GetMapping("/visit-trend")
    public Result<TrendVO> visitTrend(@RequestParam(defaultValue = "30") int days) {
        return Result.success(dashboardService.visitTrend(days));
    }

    @Operation(summary = "智能问答统计（近 N 天提问趋势、回答模式、耗时与Token）")
    @GetMapping("/qa")
    public Result<Map<String, Object>> qa(@RequestParam(defaultValue = "14") int days) {
        return Result.success(dashboardService.qaStatistics(days));
    }

    @Operation(summary = "3DGS 模型统计（格式分布、重建质量、热门文物）")
    @GetMapping("/models")
    public Result<Map<String, Object>> models() {
        return Result.success(dashboardService.modelStatistics());
    }

    @Operation(summary = "观众服务统计（预约、评分、收藏、路线、图谱实体）")
    @GetMapping("/services")
    public Result<Map<String, Object>> services() {
        return Result.success(dashboardService.serviceStatistics());
    }
}
