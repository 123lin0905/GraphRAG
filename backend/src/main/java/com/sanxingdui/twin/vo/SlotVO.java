package com.sanxingdui.twin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预约时段余量
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "预约时段余量")
public class SlotVO {
    @Schema(description = "时段")
    private String timeSlot;
    @Schema(description = "已预约人数")
    private Long booked;
    @Schema(description = "时段容量")
    private Integer capacity;
    @Schema(description = "剩余名额")
    private Long remaining;
}
