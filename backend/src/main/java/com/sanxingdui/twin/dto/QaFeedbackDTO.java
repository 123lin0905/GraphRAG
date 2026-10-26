package com.sanxingdui.twin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 问答反馈
 */
@Data
@Schema(description = "问答反馈")
public class QaFeedbackDTO {

    @Schema(description = "反馈：1-有用 -1-无用", example = "1")
    @NotNull(message = "反馈不能为空")
    private Integer feedback;
}
