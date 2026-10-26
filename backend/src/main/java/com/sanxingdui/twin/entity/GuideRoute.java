package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导览路线表（guide_route）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("guide_route")
@Schema(description = "导览路线表")
public class GuideRoute extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属博物馆ID */
    @Schema(description = "所属博物馆ID")
    private Long museumId;

    /** 路线名称 */
    @Schema(description = "路线名称")
    private String name;

    /** 路线类型：CLASSIC-经典 THEME-主题 QUICK-快速 FAMILY-亲子 */
    @Schema(description = "路线类型：CLASSIC-经典 THEME-主题 QUICK-快速 FAMILY-亲子")
    private String routeType;

    /** 路线主题 */
    @Schema(description = "路线主题")
    private String theme;

    /** 路线介绍 */
    @Schema(description = "路线介绍")
    private String description;

    /** 预计时长（分钟） */
    @Schema(description = "预计时长（分钟）")
    private Integer durationMin;

    /** 步行距离（米，约数） */
    @Schema(description = "步行距离（米，约数）")
    private Integer distanceM;

    /** 难度/强度：1-轻松 2-适中 3-较长 */
    @Schema(description = "难度/强度：1-轻松 2-适中 3-较长")
    private Integer difficulty;

    /** 适合人群 */
    @Schema(description = "适合人群")
    private String suitableCrowd;

    /** 封面图URL */
    @Schema(description = "封面图URL")
    private String coverUrl;

    /** 使用次数 */
    @Schema(description = "使用次数")
    private Integer useCount;

    /** 状态：0-停用 1-启用 */
    @Schema(description = "状态：0-停用 1-启用")
    private Integer status;
}
