package com.sanxingdui.twin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.dto.ReservationDTO;
import com.sanxingdui.twin.entity.Reservation;
import com.sanxingdui.twin.vo.SlotVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 参观预约服务
 */
public interface ReservationService extends IService<Reservation> {

    /** 某日各时段余量 */
    List<SlotVO> slots(Long museumId, LocalDate date);

    /** 创建预约（含名额校验） */
    Reservation create(ReservationDTO dto);

    /** 我的预约 */
    PageResult<Reservation> myReservations(PageQuery query, String status);

    /** 取消预约（本人） */
    void cancel(Long id);

    /** 管理端分页 */
    PageResult<Reservation> adminPage(PageQuery query, String status, LocalDate visitDate);

    /** 管理端修改状态：CONFIRMED / CANCELLED / COMPLETED（核销） */
    void changeStatus(Long id, String status);

    /** 按预约单号核销 */
    Reservation checkIn(String reservationNo);
}
