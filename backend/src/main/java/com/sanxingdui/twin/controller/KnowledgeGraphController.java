package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.entity.KgEntity;
import com.sanxingdui.twin.service.KnowledgeGraphService;
import com.sanxingdui.twin.vo.GraphVO;
import com.sanxingdui.twin.vo.NameValueVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 知识图谱查询接口（返回结构可直接用于 ECharts graph 系列）
 */
@Tag(name = "知识图谱查询", description = "全图、n跳邻居、最短路径、实体搜索、统计")
@RestController
@RequestMapping("/api/kg")
@RequiredArgsConstructor
public class KnowledgeGraphController {

    private final KnowledgeGraphService kgService;

    @Operation(summary = "知识图谱全图", description = "可按实体类型过滤（如 ARTIFACT,PIT,MATERIAL），按重要度截取前 limit 个节点")
    @GetMapping("/graph")
    public Result<GraphVO> graph(@Parameter(description = "实体类型，逗号分隔") @RequestParam(required = false) String types,
                                 @RequestParam(defaultValue = "300") int limit) {
        return Result.success(kgService.graph(types, limit));
    }

    @Operation(summary = "实体 n 跳邻居子图", description = "hops 取 1~3，maxNodes 限制节点数")
    @GetMapping("/neighbors/{entityId}")
    public Result<GraphVO> neighbors(@PathVariable Long entityId,
                                     @RequestParam(defaultValue = "1") int hops,
                                     @RequestParam(defaultValue = "80") int maxNodes) {
        return Result.success(kgService.neighbors(entityId, hops, maxNodes));
    }

    @Operation(summary = "两实体间最短关联路径")
    @GetMapping("/path")
    public Result<GraphVO> path(@RequestParam Long fromId, @RequestParam Long toId,
                                @RequestParam(defaultValue = "5") int maxDepth) {
        return Result.success(kgService.shortestPath(fromId, toId, maxDepth));
    }

    @Operation(summary = "按名称/别名搜索实体")
    @GetMapping("/search")
    public Result<List<KgEntity>> search(@RequestParam String keyword, @RequestParam(defaultValue = "20") int limit) {
        return Result.success(kgService.search(keyword, limit));
    }

    @Operation(summary = "图谱统计：实体类型分布与关系类型分布")
    @GetMapping("/statistics")
    public Result<Map<String, List<NameValueVO>>> statistics() {
        return Result.success(Map.of("entityTypes", kgService.typeStatistics(),
                "relationTypes", kgService.relationStatistics()));
    }

    @Operation(summary = "刷新图谱缓存", description = "批量导入图谱数据后调用，使 GraphRAG 立即使用最新图谱")
    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @PostMapping("/refresh")
    public Result<Void> refresh() {
        kgService.evict();
        return Result.success();
    }
}
