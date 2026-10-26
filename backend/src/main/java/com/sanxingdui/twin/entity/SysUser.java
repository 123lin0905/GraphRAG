package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 系统用户表（sys_user）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "系统用户表")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 登录用户名 */
    @Schema(description = "登录用户名")
    private String username;

    /** 登录密码（BCrypt加密） */
    @Schema(description = "登录密码（BCrypt加密）")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 昵称 */
    @Schema(description = "昵称")
    private String nickname;

    /** 头像URL */
    @Schema(description = "头像URL")
    private String avatar;

    /** 邮箱 */
    @Schema(description = "邮箱")
    private String email;

    /** 手机号 */
    @Schema(description = "手机号")
    private String phone;

    /** 性别：0-未知 1-男 2-女 */
    @Schema(description = "性别：0-未知 1-男 2-女")
    private Integer gender;

    /** 用户类型：ADMIN-管理员 CURATOR-馆员 VISITOR-游客 */
    @Schema(description = "用户类型：ADMIN-管理员 CURATOR-馆员 VISITOR-游客")
    private String userType;

    /** 账号状态：0-禁用 1-启用 */
    @Schema(description = "账号状态：0-禁用 1-启用")
    private Integer status;

    /** 最后登录时间 */
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}
