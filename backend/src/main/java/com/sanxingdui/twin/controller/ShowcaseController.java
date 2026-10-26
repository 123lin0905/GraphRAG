package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.Showcase;
import com.sanxingdui.twin.service.ShowcaseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 展柜 管理接口（通用增删改查）
 */
@Tag(name = "展柜")
@RestController
@RequestMapping("/api/showcases")
public class ShowcaseController extends BaseCrudController<Showcase, ShowcaseService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("name", "case_code");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("hallId", "hall_id");
    }
}
