package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.RestorationRecord;
import com.sanxingdui.twin.service.RestorationRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 文物修复记录 管理接口（通用增删改查）
 */
@Tag(name = "文物修复记录")
@RestController
@RequestMapping("/api/restorations")
public class RestorationRecordController extends BaseCrudController<RestorationRecord, RestorationRecordService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("restorer", "method");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("artifactId", "artifact_id", "status", "status");
    }

    @Override
    protected String defaultOrderColumn() {
        return "start_date";
    }
}
