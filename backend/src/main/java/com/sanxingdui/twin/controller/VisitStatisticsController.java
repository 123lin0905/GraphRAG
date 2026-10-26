package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.VisitStatistics;
import com.sanxingdui.twin.service.VisitStatisticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 每日访问统计 管理接口（通用增删改查）
 */
@Tag(name = "每日访问统计")
@RestController
@RequestMapping("/api/visit-statistics")
public class VisitStatisticsController extends BaseCrudController<VisitStatistics, VisitStatisticsService> {

    @Override
    protected String defaultOrderColumn() {
        return "stat_date";
    }

    @Override
    protected String[] readRoles() {
        return new String[]{"ADMIN", "CURATOR"};
    }
}
