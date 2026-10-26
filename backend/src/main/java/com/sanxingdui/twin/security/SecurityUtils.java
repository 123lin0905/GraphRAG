package com.sanxingdui.twin.security;

import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.common.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具：获取当前登录用户
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** 当前登录用户，未登录返回 null */
    public static LoginUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser user) {
            return user;
        }
        return null;
    }

    /** 当前用户ID，未登录返回 null */
    public static Long currentUserId() {
        LoginUser user = currentUser();
        return user == null ? null : user.getUserId();
    }

    /** 当前用户ID，未登录抛出 401 */
    public static Long requireUserId() {
        Long id = currentUserId();
        if (id == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return id;
    }

    /** 是否为管理员或馆员 */
    public static boolean isStaff() {
        LoginUser user = currentUser();
        return user != null && (user.getRoles().contains("ADMIN") || user.getRoles().contains("CURATOR"));
    }
}
