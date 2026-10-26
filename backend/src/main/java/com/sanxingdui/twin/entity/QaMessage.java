package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问答消息表（qa_message）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qa_message")
@Schema(description = "问答消息表")
public class QaMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    @Schema(description = "会话ID")
    private Long sessionId;

    /** 消息角色：user-用户 assistant-助手 */
    @Schema(description = "消息角色：user-用户 assistant-助手")
    private String role;

    /** 消息内容 */
    @Schema(description = "消息内容")
    private String content;

    /** GraphRAG检索到的上下文（三元组/文物/文献，JSON） */
    @Schema(description = "GraphRAG检索到的上下文（三元组/文物/文献，JSON）")
    private String retrievedContext;

    /** 引用的知识图谱实体（JSON数组） */
    @Schema(description = "引用的知识图谱实体（JSON数组）")
    private String citedEntities;

    /** 调用的大模型名称 */
    @Schema(description = "调用的大模型名称")
    private String modelName;

    /** 提示词Token数 */
    @Schema(description = "提示词Token数")
    private Integer promptTokens;

    /** 生成Token数 */
    @Schema(description = "生成Token数")
    private Integer completionTokens;

    /** 总Token数 */
    @Schema(description = "总Token数")
    private Integer totalTokens;

    /** 响应耗时（毫秒） */
    @Schema(description = "响应耗时（毫秒）")
    private Long latencyMs;

    /** 是否为离线兜底回答：0-否 1-是 */
    @Schema(description = "是否为离线兜底回答：0-否 1-是")
    private Integer isFallback;

    /** 用户反馈：1-有用 -1-无用 */
    @Schema(description = "用户反馈：1-有用 -1-无用")
    private Integer feedback;
}
