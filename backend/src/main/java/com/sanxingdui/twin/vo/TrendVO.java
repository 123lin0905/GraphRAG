package com.sanxingdui.twin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 趋势图数据：x 轴日期 + 多条序列（ECharts 折线图）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "趋势数据")
public class TrendVO {
    @Schema(description = "日期轴")
    private List<String> dates;
    @Schema(description = "序列：名称 -> 数值数组")
    private Map<String, List<Long>> series = new LinkedHashMap<>();
}
