package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.SysRole;
import com.sanxingdui.twin.service.SysRoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 角色管理 管理接口（通用增删改查）
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/roles")
public class RoleController extends BaseCrudController<SysRole, SysRoleService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("role_name", "role_code");
    }

    @Override
    protected Map<String, String> filterColumns() {
        return Map.of("status", "status");
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
