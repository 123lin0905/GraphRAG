package com.sanxingdui.twin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/**
 * 参观预约请求参数
 */
@Data
@Schema(description = "参观预约请求")
public class ReservationDTO {

    @Schema(description = "博物馆ID", example = "1")
    private Long museumId = 1L;

    @Schema(description = "参观日期 yyyy-MM-dd", example = "2026-11-01")
    @NotNull(message = "参观日期不能为空")
    @FutureOrPresent(message = "参观日期不能早于今天")
    private LocalDate visitDate;

    @Schema(description = "入馆时段", example = "09:00-11:00")
    @NotBlank(message = "入馆时段不能为空")
    private String timeSlot;

    @Schema(description = "参观人数（1-10）", example = "2")
    @NotNull(message = "参观人数不能为空")
    @Min(value = 1, message = "参观人数至少1人")
    @Max(value = 10, message = "单次最多预约10人")
    private Integer visitorCount;

    @Schema(description = "联系人姓名", example = "张三")
    @NotBlank(message = "联系人不能为空")
    private String contactName;

    @Schema(description = "联系人手机号", example = "13900000000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    @Schema(description = "意向导览路线ID")
    private Long routeId;

    @Schema(description = "是否需要人工讲解：0/1")
    private Integer needGuide = 0;

    @Schema(description = "备注")
    private String remark;
}
