package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.ArtifactImage;
import com.sanxingdui.twin.service.ArtifactImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 文物图片 管理接口（通用增删改查）
 */
@Tag(name = "文物图片")
@RestController
@RequestMapping("/api/images")
public class ArtifactImageController extends BaseCrudController<ArtifactImage, ArtifactImageService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("title");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("artifactId", "artifact_id", "imageType", "image_type");
    }
}
