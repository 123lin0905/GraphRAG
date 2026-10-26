package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.Literature;
import com.sanxingdui.twin.service.LiteratureService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 文献资料 管理接口（通用增删改查）
 */
@Tag(name = "文献资料")
@RestController
@RequestMapping("/api/literature")
public class LiteratureController extends BaseCrudController<Literature, LiteratureService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("title", "authors", "keywords", "abstract_text");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("litType", "lit_type");
    }
}
