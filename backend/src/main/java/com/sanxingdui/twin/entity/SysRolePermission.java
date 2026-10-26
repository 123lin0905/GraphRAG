package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色权限关联表（sys_role_permission）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_permission")
@Schema(description = "角色权限关联表")
public class SysRolePermission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @Schema(description = "角色ID")
    private Long roleId;

    /** 权限ID */
    @Schema(description = "权限ID")
    private Long permissionId;
}
