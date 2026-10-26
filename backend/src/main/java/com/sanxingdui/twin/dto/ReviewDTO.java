package com.sanxingdui.twin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评价请求参数
 */
@Data
@Schema(description = "发表评价请求")
public class ReviewDTO {

    @Schema(description = "评价对象类型：ARTIFACT/ROUTE/MUSEUM/QA", example = "ARTIFACT")
    @NotBlank(message = "评价对象类型不能为空")
    @Pattern(regexp = "ARTIFACT|ROUTE|MUSEUM|QA", message = "评价对象类型不合法")
    private String targetType;

    @Schema(description = "评价对象ID", example = "1")
    @NotNull(message = "评价对象ID不能为空")
    private Long targetId;

    @Schema(description = "评分1-5", example = "5")
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer rating;

    @Schema(description = "评价内容")
    @Size(max = 1000, message = "评价内容最长1000字")
    private String content;

    @Schema(description = "图片URL，逗号分隔")
    private String images;
}
