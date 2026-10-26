package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.annotation.OperationLog;
import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.dto.LoginDTO;
import com.sanxingdui.twin.dto.PasswordChangeDTO;
import com.sanxingdui.twin.dto.RegisterDTO;
import com.sanxingdui.twin.service.AuthService;
import com.sanxingdui.twin.util.IpUtils;
import com.sanxingdui.twin.vo.LoginVO;
import com.sanxingdui.twin.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 */
@Tag(name = "认证管理", description = "登录、注册、当前用户")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "返回 JWT 令牌，后续请求在请求头携带 Authorization: Bearer {token}")
    @OperationLog(module = "认证", value = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        return Result.success("登录成功", authService.login(dto, IpUtils.getClientIp(request)));
    }

    @Operation(summary = "游客注册")
    @OperationLog(module = "认证", value = "游客注册")
    @PostMapping("/register")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success("注册成功", authService.register(dto));
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        return Result.success(authService.currentUserInfo());
    }

    @Operation(summary = "修改密码")
    @OperationLog(module = "认证", value = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeDTO dto) {
        authService.changePassword(dto);
        return Result.success();
    }

    @Operation(summary = "退出登录", description = "JWT 为无状态令牌，前端删除本地令牌即可")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
