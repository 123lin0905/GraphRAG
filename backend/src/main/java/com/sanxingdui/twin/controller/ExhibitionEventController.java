package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.ExhibitionEvent;
import com.sanxingdui.twin.service.ExhibitionEventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 展览活动 管理接口（通用增删改查）
 */
@Tag(name = "展览活动")
@RestController
@RequestMapping("/api/events")
public class ExhibitionEventController extends BaseCrudController<ExhibitionEvent, ExhibitionEventService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("title", "content", "location");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("eventType", "event_type", "status", "status", "hallId", "hall_id");
    }

    @Override
    protected String defaultOrderColumn() {
        return "start_time";
    }
}
