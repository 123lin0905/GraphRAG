package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.ArtifactCategory;
import com.sanxingdui.twin.service.ArtifactCategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 文物分类 管理接口（通用增删改查）
 */
@Tag(name = "文物分类")
@RestController
@RequestMapping("/api/categories")
public class ArtifactCategoryController extends BaseCrudController<ArtifactCategory, ArtifactCategoryService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("name", "code");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("parentId", "parent_id");
    }
}
