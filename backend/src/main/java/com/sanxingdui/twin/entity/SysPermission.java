package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限表（sys_permission）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
@Schema(description = "权限表")
public class SysPermission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 父级权限ID，0表示顶级 */
    @Schema(description = "父级权限ID，0表示顶级")
    private Long parentId;

    /** 权限标识（如 artifact:edit） */
    @Schema(description = "权限标识（如 artifact:edit）")
    private String permCode;

    /** 权限名称 */
    @Schema(description = "权限名称")
    private String permName;

    /** 权限类型：MENU-菜单 BUTTON-按钮 API-接口 */
    @Schema(description = "权限类型：MENU-菜单 BUTTON-按钮 API-接口")
    private String permType;

    /** 前端路由或接口路径 */
    @Schema(description = "前端路由或接口路径")
    private String path;

    /** 菜单图标 */
    @Schema(description = "菜单图标")
    private String icon;

    /** 排序号 */
    @Schema(description = "排序号")
    private Integer sort;
}
