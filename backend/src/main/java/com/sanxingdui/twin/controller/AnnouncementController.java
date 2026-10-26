package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.Announcement;
import com.sanxingdui.twin.service.AnnouncementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 公告 管理接口（通用增删改查）
 */
@Tag(name = "公告")
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController extends BaseCrudController<Announcement, AnnouncementService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("title", "content");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("annType", "ann_type", "status", "status");
    }

    @Override
    protected String defaultOrderColumn() {
        return "publish_time";
    }
}
