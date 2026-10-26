package com.sanxingdui.twin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 大模型接口配置属性（application.yml 中 llm.*），兼容 OpenAI Chat Completions 协议
 */
@Data
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {
    /** 接口基础地址，如 https://api.deepseek.com 或 https://api.openai.com/v1 */
    private String baseUrl = "https://api.deepseek.com";
    /** API Key，为空时问答服务降级为离线兜底回答 */
    private String apiKey;
    /** 模型名称 */
    private String model = "deepseek-chat";
    /** 采样温度 */
    private double temperature = 0.3;
    /** 最大生成 Token 数 */
    private int maxTokens = 1024;
    /** 连接超时（秒） */
    private int connectTimeoutSeconds = 10;
    /** 读取超时（秒） */
    private int readTimeoutSeconds = 90;
}
