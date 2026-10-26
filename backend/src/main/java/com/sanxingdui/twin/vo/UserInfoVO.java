package com.sanxingdui.twin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前用户信息
 */
@Data
@Schema(description = "用户信息")
public class UserInfoVO {
    @Schema(description = "用户ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像")
    private String avatar;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "用户类型")
    private String userType;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
    @Schema(description = "注册时间")
    private LocalDateTime createdAt;
    @Schema(description = "角色编码")
    private List<String> roles;
    @Schema(description = "权限标识")
    private List<String> permissions;
}
