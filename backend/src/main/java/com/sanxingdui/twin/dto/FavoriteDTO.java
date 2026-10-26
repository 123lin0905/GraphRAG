package com.sanxingdui.twin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 收藏/取消收藏请求参数
 */
@Data
@Schema(description = "收藏请求")
public class FavoriteDTO {

    @Schema(description = "收藏对象类型：ARTIFACT/ROUTE", example = "ARTIFACT")
    @Pattern(regexp = "ARTIFACT|ROUTE", message = "收藏对象类型不合法")
    private String targetType = "ARTIFACT";

    @Schema(description = "收藏对象ID", example = "1")
    @NotNull(message = "收藏对象ID不能为空")
    private Long targetId;
}
