package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.GuideRouteNode;
import com.sanxingdui.twin.service.GuideRouteNodeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 导览路线节点 管理接口（通用增删改查）
 */
@Tag(name = "导览路线节点")
@RestController
@RequestMapping("/api/route-nodes")
public class GuideRouteNodeController extends BaseCrudController<GuideRouteNode, GuideRouteNodeService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("title");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("routeId", "route_id", "artifactId", "artifact_id");
    }
}
