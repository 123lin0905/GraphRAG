package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.entity.GuideRoute;
import com.sanxingdui.twin.service.GuideRouteService;
import com.sanxingdui.twin.vo.RouteDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 导览路线接口
 */
@Tag(name = "导览路线")
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class GuideRouteController extends BaseCrudController<GuideRoute, GuideRouteService> {

    private final GuideRouteService routeService;

    @Operation(summary = "路线详情（含有序节点、讲解词、3D漫游相机位、文物模型）")
    @GetMapping("/{id}/detail")
    public Result<RouteDetailVO> detail(@PathVariable Long id) {
        return Result.success(routeService.detail(id));
    }

    @Operation(summary = "路线推荐", description = "interests：兴趣标签逗号分隔（如 面具,太阳）；minutes：可用时长")
    @GetMapping("/recommend")
    public Result<List<GuideRoute>> recommend(@RequestParam(required = false) String interests,
                                              @RequestParam(required = false) Integer minutes) {
        return Result.success(routeService.recommend(interests, minutes));
    }

    @Operation(summary = "开始导览（记录使用次数，匿名可用）")
    @PostMapping("/{id}/start")
    public Result<Void> start(@PathVariable Long id) {
        routeService.start(id);
        return Result.success();
    }

    @Override
    protected List<String> keywordColumns() {
        return List.of("name", "theme", "description");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("routeType", "route_type", "status", "status", "museumId", "museum_id");
    }
}
