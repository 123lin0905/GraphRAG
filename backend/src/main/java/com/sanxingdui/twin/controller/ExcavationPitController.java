package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.ExcavationPit;
import com.sanxingdui.twin.service.ExcavationPitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 祭祀坑 管理接口（通用增删改查）
 */
@Tag(name = "祭祀坑")
@RestController
@RequestMapping("/api/pits")
public class ExcavationPitController extends BaseCrudController<ExcavationPit, ExcavationPitService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("name", "pit_code", "main_findings");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("periodId", "period_id");
    }
}
