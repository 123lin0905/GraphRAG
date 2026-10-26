package com.sanxingdui.twin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GraphRAG 检索参数（application.yml 中 rag.*）
 */
@Data
@ConfigurationProperties(prefix = "rag")
public class RagProperties {
    /** 子图扩展最大跳数（1~2） */
    private int maxHops = 2;
    /** 注入提示词的最大三元组数 */
    private int maxTriples = 40;
    /** 注入提示词的最大文献片段数 */
    private int maxLiterature = 3;
    /** 注入提示词的最大文物描述数 */
    private int maxArtifacts = 4;
    /** 携带的历史对话轮数 */
    private int historyTurns = 3;
}
