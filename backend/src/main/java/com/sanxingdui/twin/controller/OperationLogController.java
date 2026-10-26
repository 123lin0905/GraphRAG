package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.SysOperationLog;
import com.sanxingdui.twin.service.SysOperationLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 操作日志 管理接口（通用增删改查）
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/logs")
public class OperationLogController extends BaseCrudController<SysOperationLog, SysOperationLogService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("username", "operation", "request_uri", "module");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("userId", "user_id", "status", "status", "module", "module");
    }

    @Override
    protected String defaultOrderColumn() {
        return "created_at";
    }

    @Override
    protected String[] writeRoles() {
        return new String[]{"ADMIN"};
    }

    @Override
    protected String[] readRoles() {
        return new String[]{"ADMIN"};
    }
}
