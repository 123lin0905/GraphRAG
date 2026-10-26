package com.sanxingdui.twin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.common.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 未认证 / 无权限时返回统一 JSON，而不是默认的 HTML 页面
 */
@Component
@RequiredArgsConstructor
public class RestAuthHandlers {

    private final ObjectMapper objectMapper;

    public AuthenticationEntryPoint entryPoint() {
        return (request, response, e) -> write(response, HttpServletResponse.SC_UNAUTHORIZED, ResultCode.UNAUTHORIZED);
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, e) -> write(response, HttpServletResponse.SC_FORBIDDEN, ResultCode.FORBIDDEN);
    }

    private void write(HttpServletResponse response, int status, ResultCode code) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code)));
    }
}
