package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 祭祀坑（出土单位）表（excavation_pit）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("excavation_pit")
@Schema(description = "祭祀坑（出土单位）表")
public class ExcavationPit extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 坑号（如 K1） */
    @Schema(description = "坑号（如 K1）")
    private String pitCode;

    /** 名称（如 一号祭祀坑） */
    @Schema(description = "名称（如 一号祭祀坑）")
    private String name;

    /** 发现年份 */
    @Schema(description = "发现年份")
    private Integer discoveryYear;

    /** 发掘时间段 */
    @Schema(description = "发掘时间段")
    private String excavationPeriod;

    /** 坑口长（米，约数） */
    @Schema(description = "坑口长（米，约数）")
    private BigDecimal lengthM;

    /** 坑口宽（米，约数） */
    @Schema(description = "坑口宽（米，约数）")
    private BigDecimal widthM;

    /** 面积（平方米，约数） */
    @Schema(description = "面积（平方米，约数）")
    private BigDecimal areaSqm;

    /** 所属年代/文化期ID */
    @Schema(description = "所属年代/文化期ID")
    private Long periodId;

    /** 坑位概况 */
    @Schema(description = "坑位概况")
    private String description;

    /** 主要出土器物概述 */
    @Schema(description = "主要出土器物概述")
    private String mainFindings;

    /** 遗址平面示意图X坐标 */
    @Schema(description = "遗址平面示意图X坐标")
    private BigDecimal posX;

    /** 遗址平面示意图Y坐标 */
    @Schema(description = "遗址平面示意图Y坐标")
    private BigDecimal posY;
}
