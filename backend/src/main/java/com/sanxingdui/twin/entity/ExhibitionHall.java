package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 展厅表（exhibition_hall）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exhibition_hall")
@Schema(description = "展厅表")
public class ExhibitionHall extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属博物馆ID */
    @Schema(description = "所属博物馆ID")
    private Long museumId;

    /** 展厅编号 */
    @Schema(description = "展厅编号")
    private String hallCode;

    /** 展厅名称 */
    @Schema(description = "展厅名称")
    private String name;

    /** 展陈主题 */
    @Schema(description = "展陈主题")
    private String theme;

    /** 所在楼层 */
    @Schema(description = "所在楼层")
    private Integer floor;

    /** 面积（平方米） */
    @Schema(description = "面积（平方米）")
    private BigDecimal area;

    /** 展厅介绍 */
    @Schema(description = "展厅介绍")
    private String description;

    /** 封面图URL */
    @Schema(description = "封面图URL")
    private String coverUrl;

    /** 展厅三维场景文件URL（3DGS/全景） */
    @Schema(description = "展厅三维场景文件URL（3DGS/全景）")
    private String sceneUrl;

    /** 平面导览图X坐标 */
    @Schema(description = "平面导览图X坐标")
    private BigDecimal mapX;

    /** 平面导览图Y坐标 */
    @Schema(description = "平面导览图Y坐标")
    private BigDecimal mapY;

    /** 排序号 */
    @Schema(description = "排序号")
    private Integer sort;

    /** 状态：0-闭馆 1-开放 */
    @Schema(description = "状态：0-闭馆 1-开放")
    private Integer status;
}
