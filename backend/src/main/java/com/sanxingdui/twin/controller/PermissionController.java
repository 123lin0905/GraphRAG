package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.SysPermission;
import com.sanxingdui.twin.service.SysPermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 权限管理 管理接口（通用增删改查）
 */
@Tag(name = "权限管理")
@RestController
@RequestMapping("/api/permissions")
public class PermissionController extends BaseCrudController<SysPermission, SysPermissionService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("perm_name", "perm_code");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("parentId", "parent_id", "permType", "perm_type");
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
