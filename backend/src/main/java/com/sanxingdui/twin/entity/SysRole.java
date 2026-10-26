package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色表（sys_role）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "角色表")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 角色编码（如 ADMIN） */
    @Schema(description = "角色编码（如 ADMIN）")
    private String roleCode;

    /** 角色名称 */
    @Schema(description = "角色名称")
    private String roleName;

    /** 角色描述 */
    @Schema(description = "角色描述")
    private String description;

    /** 排序号 */
    @Schema(description = "排序号")
    private Integer sort;

    /** 状态：0-禁用 1-启用 */
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;
}
