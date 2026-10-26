package com.sanxingdui.twin.security;

import com.sanxingdui.twin.config.JwtProperties;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器：从请求头解析令牌并写入安全上下文。
 * 令牌无效时不直接拒绝，交由后续授权规则决定（以便公开接口可匿名访问）。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Claims claims = jwtUtil.parse(token);
            if (claims != null) {
                try {
                    // 每次请求重新加载用户，保证禁用账号、角色变更即时生效
                    LoginUser user = userDetailsService.loadUserByUsername(claims.getSubject());
                    if (user.isEnabled()) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (UsernameNotFoundException ignored) {
                    // 用户已被删除，按匿名处理
                }
            }
        }
        chain.doFilter(request, response);
    }

    /** 支持 Authorization 请求头，以及 SSE(EventSource) 场景下的 token 查询参数 */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(jwtProperties.getHeader());
        if (StringUtils.hasText(header) && header.startsWith(jwtProperties.getPrefix())) {
            return header.substring(jwtProperties.getPrefix().length()).trim();
        }
        String param = request.getParameter("token");
        return StringUtils.hasText(param) ? param : null;
    }
}
