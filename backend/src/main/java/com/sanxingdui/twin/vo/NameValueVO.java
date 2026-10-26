package com.sanxingdui.twin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 名称-数值对（ECharts 饼图/柱状图通用数据结构）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "名称-数值对")
public class NameValueVO {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "数值")
    private Long value;
}
