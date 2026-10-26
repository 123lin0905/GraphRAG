package com.sanxingdui.twin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 游客注册请求参数
 */
@Data
@Schema(description = "注册请求")
public class RegisterDTO {

    @Schema(description = "用户名（4-20位字母、数字或下划线）", example = "tourist01")
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{4,20}$", message = "用户名须为4-20位字母、数字或下划线")
    private String username;

    @Schema(description = "密码（6-32位）", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度须为6-32位")
    private String password;

    @Schema(description = "昵称", example = "古蜀爱好者")
    @Size(max = 50, message = "昵称最长50个字符")
    private String nickname;

    @Schema(description = "手机号", example = "13900000000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;
}
