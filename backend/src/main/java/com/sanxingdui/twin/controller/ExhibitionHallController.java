package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.ExhibitionHall;
import com.sanxingdui.twin.service.ExhibitionHallService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 展厅 管理接口（通用增删改查）
 */
@Tag(name = "展厅")
@RestController
@RequestMapping("/api/halls")
public class ExhibitionHallController extends BaseCrudController<ExhibitionHall, ExhibitionHallService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("name", "theme", "hall_code");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("museumId", "museum_id", "status", "status");
    }
}
