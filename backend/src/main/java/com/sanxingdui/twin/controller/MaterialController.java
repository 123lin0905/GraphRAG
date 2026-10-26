package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.Material;
import com.sanxingdui.twin.service.MaterialService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 材质 管理接口（通用增删改查）
 */
@Tag(name = "材质")
@RestController
@RequestMapping("/api/materials")
public class MaterialController extends BaseCrudController<Material, MaterialService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("name", "category");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("category", "category");
    }
}
