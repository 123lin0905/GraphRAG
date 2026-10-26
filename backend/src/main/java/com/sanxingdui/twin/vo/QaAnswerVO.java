package com.sanxingdui.twin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 智能问答结果
 */
@Data
@Schema(description = "智能问答结果")
public class QaAnswerVO {
    @Schema(description = "会话ID")
    private Long sessionId;
    @Schema(description = "助手消息ID")
    private Long messageId;
    @Schema(description = "回答内容（Markdown）")
    private String answer;
    @Schema(description = "是否为离线兜底回答")
    private Boolean fallback;
    @Schema(description = "模型名称")
    private String model;
    @Schema(description = "耗时（毫秒）")
    private Long latencyMs;
    @Schema(description = "提示词Token")
    private Integer promptTokens;
    @Schema(description = "生成Token")
    private Integer completionTokens;
    @Schema(description = "总Token")
    private Integer totalTokens;
    @Schema(description = "引用的知识图谱实体名称")
    private List<String> citedEntities;
    @Schema(description = "检索到的三元组（头-关系-尾）")
    private List<String> triples;
    @Schema(description = "引用的文献标题")
    private List<String> references;
    @Schema(description = "检索子图（可直接用于ECharts渲染）")
    private GraphVO subgraph;
}
