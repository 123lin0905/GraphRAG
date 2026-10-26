package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.KgRelation;
import com.sanxingdui.twin.service.KgRelationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 知识图谱-关系 管理接口（通用增删改查）
 */
@Tag(name = "知识图谱-关系")
@RestController
@RequestMapping("/api/kg/relations")
public class KgRelationController extends BaseCrudController<KgRelation, KgRelationService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("relation_name", "relation_type", "description");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("sourceId", "source_id", "targetId", "target_id", "relationType", "relation_type");
    }
}
