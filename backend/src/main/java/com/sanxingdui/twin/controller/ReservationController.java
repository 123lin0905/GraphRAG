package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.annotation.OperationLog;
import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.dto.ReservationDTO;
import com.sanxingdui.twin.entity.Reservation;
import com.sanxingdui.twin.service.ReservationService;
import com.sanxingdui.twin.vo.SlotVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 参观预约接口
 */
@Tag(name = "参观预约")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "查询某日各时段余量（公开）")
    @GetMapping("/slots")
    public Result<List<SlotVO>> slots(@RequestParam(defaultValue = "1") Long museumId,
                                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(reservationService.slots(museumId, date));
    }

    @Operation(summary = "提交预约")
    @OperationLog(module = "预约", value = "提交预约")
    @PostMapping
    public Result<Reservation> create(@Valid @RequestBody ReservationDTO dto) {
        return Result.success("预约成功", reservationService.create(dto));
    }

    @Operation(summary = "我的预约")
    @GetMapping("/my")
    public Result<PageResult<Reservation>> my(@Valid PageQuery query, @RequestParam(required = false) String status) {
        return Result.success(reservationService.myReservations(query, status));
    }

    @Operation(summary = "取消预约")
    @OperationLog(module = "预约", value = "取消预约")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return Result.success();
    }

    @Operation(summary = "管理端：预约分页", description = "keyword 匹配单号/联系人/手机号")
    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @GetMapping("/page")
    public Result<PageResult<Reservation>> page(@Valid PageQuery query,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate visitDate) {
        return Result.success(reservationService.adminPage(query, status, visitDate));
    }

    @Operation(summary = "管理端：修改预约状态", description = "status：CONFIRMED / CANCELLED / COMPLETED")
    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @OperationLog(module = "预约", value = "修改预约状态")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam String status) {
        reservationService.changeStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "管理端：按单号核销入馆")
    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @OperationLog(module = "预约", value = "核销入馆")
    @PostMapping("/check-in")
    public Result<Reservation> checkIn(@RequestParam String reservationNo) {
        return Result.success("核销成功", reservationService.checkIn(reservationNo));
    }
}
