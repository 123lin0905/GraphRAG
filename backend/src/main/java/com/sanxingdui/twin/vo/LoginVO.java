package com.sanxingdui.twin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录结果")
public class LoginVO {
    @Schema(description = "JWT令牌")
    private String token;
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;
    @Schema(description = "有效期（秒）")
    private long expiresIn;
    @Schema(description = "用户信息")
    private UserInfoVO user;
}
