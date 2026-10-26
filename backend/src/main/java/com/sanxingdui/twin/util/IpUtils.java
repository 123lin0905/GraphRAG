package com.sanxingdui.twin.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 客户端 IP 工具（兼容 Nginx 反向代理）
 */
public final class IpUtils {

    private IpUtils() {
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        for (String header : new String[]{"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP"}) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // 多级代理取第一个
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
