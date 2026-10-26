package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 问答会话表（qa_session）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qa_session")
@Schema(description = "问答会话表")
public class QaSession extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID（匿名游客为空） */
    @Schema(description = "用户ID（匿名游客为空）")
    private Long userId;

    /** 会话标题（取首个问题） */
    @Schema(description = "会话标题（取首个问题）")
    private String title;

    /** 客户端IP */
    @Schema(description = "客户端IP")
    private String clientIp;

    /** 消息条数 */
    @Schema(description = "消息条数")
    private Integer messageCount;

    /** 最近活跃时间 */
    @Schema(description = "最近活跃时间")
    private LocalDateTime lastActiveTime;
}
