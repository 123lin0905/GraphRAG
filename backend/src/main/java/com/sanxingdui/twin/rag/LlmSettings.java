package com.sanxingdui.twin.rag;

import org.springframework.util.StringUtils;

/**
 * 运行时生效的大模型配置（数据库 system_config 优先，application.yml 兜底）
 *
 * @param baseUrl     接口基础地址
 * @param apiKey      API Key
 * @param model       模型名称
 * @param temperature 采样温度
 * @param maxTokens   最大生成Token
 */
public record LlmSettings(String baseUrl, String apiKey, String model, double temperature, int maxTokens) {

    /** 是否已配置可用的 API Key */
    public boolean configured() {
        return StringUtils.hasText(apiKey) && StringUtils.hasText(baseUrl);
    }

    /** Chat Completions 完整地址 */
    public String chatUrl() {
        String base = baseUrl.trim().replaceAll("/+$", "");
        return base.endsWith("/chat/completions") ? base : base + "/chat/completions";
    }
}
