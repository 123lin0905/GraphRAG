package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.KgEntity;
import com.sanxingdui.twin.service.KgEntityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 知识图谱-实体 管理接口（通用增删改查）
 */
@Tag(name = "知识图谱-实体")
@RestController
@RequestMapping("/api/kg/entities")
public class KgEntityController extends BaseCrudController<KgEntity, KgEntityService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("name", "aliases", "description");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("entityType", "entity_type", "refTable", "ref_table", "refId", "ref_id");
    }
}
