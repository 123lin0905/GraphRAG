package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 展柜表（showcase）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("showcase")
@Schema(description = "展柜表")
public class Showcase extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属展厅ID */
    @Schema(description = "所属展厅ID")
    private Long hallId;

    /** 展柜编号 */
    @Schema(description = "展柜编号")
    private String caseCode;

    /** 展柜名称 */
    @Schema(description = "展柜名称")
    private String name;

    /** 展柜类型：独立柜/通柜/壁柜/中心柜 */
    @Schema(description = "展柜类型：独立柜/通柜/壁柜/中心柜")
    private String caseType;

    /** 展厅坐标系X（米） */
    @Schema(description = "展厅坐标系X（米）")
    private BigDecimal posX;

    /** 展厅坐标系Y（米） */
    @Schema(description = "展厅坐标系Y（米）")
    private BigDecimal posY;

    /** 展厅坐标系Z（米） */
    @Schema(description = "展厅坐标系Z（米）")
    private BigDecimal posZ;

    /** 柜内温度（℃，环境监测） */
    @Schema(description = "柜内温度（℃，环境监测）")
    private BigDecimal temperature;

    /** 柜内相对湿度（%，环境监测） */
    @Schema(description = "柜内相对湿度（%，环境监测）")
    private BigDecimal humidity;
}
