package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.Model3d;
import com.sanxingdui.twin.service.Model3dService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 三维模型(3DGS) 管理接口（通用增删改查）
 */
@Tag(name = "三维模型(3DGS)")
@RestController
@RequestMapping("/api/models")
public class Model3dController extends BaseCrudController<Model3d, Model3dService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("name", "trained_by");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("artifactId", "artifact_id", "hallId", "hall_id", "format", "format", "modelType", "model_type", "status", "status");
    }
}
