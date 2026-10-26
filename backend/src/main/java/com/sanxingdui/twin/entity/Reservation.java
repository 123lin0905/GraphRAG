package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 参观预约表（reservation）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reservation")
@Schema(description = "参观预约表")
public class Reservation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 预约单号 */
    @Schema(description = "预约单号")
    private String reservationNo;

    /** 预约用户ID */
    @Schema(description = "预约用户ID")
    private Long userId;

    /** 博物馆ID */
    @Schema(description = "博物馆ID")
    private Long museumId;

    /** 参观日期 */
    @Schema(description = "参观日期")
    private LocalDate visitDate;

    /** 入馆时段（如 09:00-11:00） */
    @Schema(description = "入馆时段（如 09:00-11:00）")
    private String timeSlot;

    /** 参观人数 */
    @Schema(description = "参观人数")
    private Integer visitorCount;

    /** 联系人姓名 */
    @Schema(description = "联系人姓名")
    private String contactName;

    /** 联系人手机号 */
    @Schema(description = "联系人手机号")
    private String contactPhone;

    /** 意向导览路线ID */
    @Schema(description = "意向导览路线ID")
    private Long routeId;

    /** 是否需要人工讲解：0-否 1-是 */
    @Schema(description = "是否需要人工讲解：0-否 1-是")
    private Integer needGuide;

    /** 状态：PENDING-待确认 CONFIRMED-已确认 CANCELLED-已取消 COMPLETED-已核销 */
    @Schema(description = "状态：PENDING-待确认 CONFIRMED-已确认 CANCELLED-已取消 COMPLETED-已核销")
    private String status;

    /** 核销入馆时间 */
    @Schema(description = "核销入馆时间")
    private LocalDateTime checkInTime;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}
