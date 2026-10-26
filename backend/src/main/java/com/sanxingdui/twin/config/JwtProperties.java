package com.sanxingdui.twin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性（application.yml 中 jwt.*）
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /** 签名密钥 */
    private String secret;
    /** 有效期（小时） */
    private long expireHours = 24;
    /** 请求头名称 */
    private String header = "Authorization";
    /** 令牌前缀 */
    private String prefix = "Bearer ";
}
