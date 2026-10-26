package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 博物馆表（museum）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("museum")
@Schema(description = "博物馆表")
public class Museum extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 博物馆名称 */
    @Schema(description = "博物馆名称")
    private String name;

    /** 简称 */
    @Schema(description = "简称")
    private String shortName;

    /** 地址 */
    @Schema(description = "地址")
    private String address;

    /** 经度 */
    @Schema(description = "经度")
    private BigDecimal longitude;

    /** 纬度 */
    @Schema(description = "纬度")
    private BigDecimal latitude;

    /** 开放时间说明 */
    @Schema(description = "开放时间说明")
    private String openTime;

    /** 联系电话 */
    @Schema(description = "联系电话")
    private String phone;

    /** 博物馆简介 */
    @Schema(description = "博物馆简介")
    private String introduction;

    /** 封面图URL */
    @Schema(description = "封面图URL")
    private String coverUrl;

    /** 每日最大接待量（人） */
    @Schema(description = "每日最大接待量（人）")
    private Integer dailyCapacity;
}
