package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 年代文化期表（dynasty_period）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dynasty_period")
@Schema(description = "年代文化期表")
public class DynastyPeriod extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 年代或文化期名称 */
    @Schema(description = "年代或文化期名称")
    private String name;

    /** 对应考古学文化 */
    @Schema(description = "对应考古学文化")
    private String cultureName;

    /** 起始年份（公元前为负数，约数） */
    @Schema(description = "起始年份（公元前为负数，约数）")
    private Integer startYear;

    /** 结束年份（公元前为负数，约数） */
    @Schema(description = "结束年份（公元前为负数，约数）")
    private Integer endYear;

    /** 年代文字描述（如 距今约4500-3700年） */
    @Schema(description = "年代文字描述（如 距今约4500-3700年）")
    private String eraDesc;

    /** 文化期介绍 */
    @Schema(description = "文化期介绍")
    private String description;

    /** 排序号 */
    @Schema(description = "排序号")
    private Integer sort;
}
