package com.sanxingdui.twin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 智能问答请求参数
 */
@Data
@Schema(description = "智能问答请求")
public class QaAskDTO {

    @Schema(description = "会话ID，为空则自动创建新会话")
    private Long sessionId;

    @Schema(description = "用户问题", example = "青铜神树出土于哪个祭祀坑？有什么含义？")
    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题最长500个字符")
    private String question;
}
