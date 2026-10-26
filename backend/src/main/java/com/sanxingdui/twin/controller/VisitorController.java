package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.Visitor;
import com.sanxingdui.twin.service.VisitorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 游客档案 管理接口（通用增删改查）
 */
@Tag(name = "游客档案")
@RestController
@RequestMapping("/api/visitors")
public class VisitorController extends BaseCrudController<Visitor, VisitorService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("real_name", "region", "interests");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("visitorType", "visitor_type", "userId", "user_id");
    }

    @Override
    protected String[] readRoles() {
        return new String[]{"ADMIN", "CURATOR"};
    }
}
