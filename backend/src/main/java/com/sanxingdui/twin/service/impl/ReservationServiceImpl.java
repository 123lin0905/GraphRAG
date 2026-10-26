package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.common.ResultCode;
import com.sanxingdui.twin.dto.ReservationDTO;
import com.sanxingdui.twin.entity.Reservation;
import com.sanxingdui.twin.entity.Visitor;
import com.sanxingdui.twin.mapper.ReservationMapper;
import com.sanxingdui.twin.mapper.VisitorMapper;
import com.sanxingdui.twin.security.SecurityUtils;
import com.sanxingdui.twin.service.ReservationService;
import com.sanxingdui.twin.service.SystemConfigService;
import com.sanxingdui.twin.vo.SlotVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 参观预约服务实现
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    private static final String DEFAULT_SLOTS = "09:00-11:00,11:00-13:00,13:00-15:00,15:00-17:00";
    /** 占用名额的状态 */
    private static final List<String> ACTIVE_STATUS = List.of("PENDING", "CONFIRMED", "COMPLETED");
    private static final Set<String> ADMIN_STATUS = Set.of("CONFIRMED", "CANCELLED", "COMPLETED");
    /** 最多可提前预约天数 */
    private static final int MAX_ADVANCE_DAYS = 30;

    private final SystemConfigService configService;
    private final VisitorMapper visitorMapper;

    @Override
    public List<SlotVO> slots(Long museumId, LocalDate date) {
        int capacity = configService.getInt("reservation.slot_capacity", 300);
        List<String> slots = Arrays.stream(configService.getValue("reservation.time_slots", DEFAULT_SLOTS).split(","))
                .map(String::trim).filter(StringUtils::hasText).toList();
        List<Map<String, Object>> rows = baseMapper.selectMaps(new QueryWrapper<Reservation>()
                .select("time_slot AS slot", "IFNULL(SUM(visitor_count), 0) AS booked")
                .eq("museum_id", museumId == null ? 1L : museumId).eq("visit_date", date)
                .in("status", ACTIVE_STATUS).groupBy("time_slot"));
        return slots.stream().map(slot -> {
            long booked = rows.stream().filter(r -> slot.equals(r.get("slot")))
                    .mapToLong(r -> ((Number) r.get("booked")).longValue()).sum();
            return new SlotVO(slot, booked, capacity, Math.max(capacity - booked, 0));
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reservation create(ReservationDTO dto) {
        Long userId = SecurityUtils.requireUserId();
        if (dto.getVisitDate().isAfter(LocalDate.now().plusDays(MAX_ADVANCE_DAYS))) {
            throw new BusinessException("最多可提前" + MAX_ADVANCE_DAYS + "天预约");
        }
        Long museumId = dto.getMuseumId() == null ? 1L : dto.getMuseumId();
        SlotVO slot = slots(museumId, dto.getVisitDate()).stream()
                .filter(s -> s.getTimeSlot().equals(dto.getTimeSlot())).findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST, "无效的入馆时段：" + dto.getTimeSlot()));
        if (slot.getRemaining() < dto.getVisitorCount()) {
            throw new BusinessException("该时段剩余名额不足，仅剩 " + slot.getRemaining() + " 个");
        }
        // 同一用户同一天同一时段只能有一个有效预约
        long dup = count(new LambdaQueryWrapper<Reservation>().eq(Reservation::getUserId, userId)
                .eq(Reservation::getVisitDate, dto.getVisitDate()).eq(Reservation::getTimeSlot, dto.getTimeSlot())
                .in(Reservation::getStatus, "PENDING", "CONFIRMED"));
        if (dup > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "您已预约该时段，请勿重复预约");
        }
        Reservation r = new Reservation();
        BeanUtils.copyProperties(dto, r);
        r.setMuseumId(museumId);
        r.setUserId(userId);
        r.setReservationNo(generateNo());
        r.setStatus("CONFIRMED");
        r.setNeedGuide(dto.getNeedGuide() == null ? 0 : dto.getNeedGuide());
        save(r);
        return r;
    }

    @Override
    public PageResult<Reservation> myReservations(PageQuery query, String status) {
        Long userId = SecurityUtils.requireUserId();
        return PageResult.of(page(query.toPage(), new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .eq(StringUtils.hasText(status), Reservation::getStatus, status)
                .orderByDesc(Reservation::getVisitDate).orderByDesc(Reservation::getId)));
    }

    @Override
    public void cancel(Long id) {
        Long userId = SecurityUtils.requireUserId();
        Reservation r = getById(id);
        if (r == null || !r.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "预约不存在");
        }
        if (!"PENDING".equals(r.getStatus()) && !"CONFIRMED".equals(r.getStatus())) {
            throw new BusinessException("当前状态不可取消");
        }
        if (r.getVisitDate().isBefore(LocalDate.now())) {
            throw new BusinessException("已过参观日期，无法取消");
        }
        Reservation update = new Reservation();
        update.setId(id);
        update.setStatus("CANCELLED");
        updateById(update);
    }

    @Override
    public PageResult<Reservation> adminPage(PageQuery query, String status, LocalDate visitDate) {
        String kw = query.getKeyword();
        return PageResult.of(page(query.toPage(), new LambdaQueryWrapper<Reservation>()
                .and(StringUtils.hasText(kw), w -> w.like(Reservation::getReservationNo, kw)
                        .or().like(Reservation::getContactName, kw).or().like(Reservation::getContactPhone, kw))
                .eq(StringUtils.hasText(status), Reservation::getStatus, status)
                .eq(visitDate != null, Reservation::getVisitDate, visitDate)
                .orderByDesc(Reservation::getVisitDate).orderByDesc(Reservation::getId)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, String status) {
        if (!ADMIN_STATUS.contains(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "状态不合法");
        }
        Reservation r = getById(id);
        if (r == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "预约不存在");
        }
        if ("COMPLETED".equals(status)) {
            doCheckIn(r);
            return;
        }
        Reservation update = new Reservation();
        update.setId(id);
        update.setStatus(status);
        updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reservation checkIn(String reservationNo) {
        Reservation r = getOne(new LambdaQueryWrapper<Reservation>().eq(Reservation::getReservationNo, reservationNo));
        if (r == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "预约单不存在");
        }
        doCheckIn(r);
        return getById(r.getId());
    }

    /** 核销：校验状态与日期，记录入馆时间并累计游客到馆次数 */
    private void doCheckIn(Reservation r) {
        if (!"CONFIRMED".equals(r.getStatus()) && !"PENDING".equals(r.getStatus())) {
            throw new BusinessException("该预约当前状态为 " + r.getStatus() + "，无法核销");
        }
        if (!r.getVisitDate().equals(LocalDate.now())) {
            throw new BusinessException("仅可在参观当日核销");
        }
        Reservation update = new Reservation();
        update.setId(r.getId());
        update.setStatus("COMPLETED");
        update.setCheckInTime(LocalDateTime.now());
        updateById(update);
        Visitor visitor = visitorMapper.selectOne(new LambdaQueryWrapper<Visitor>().eq(Visitor::getUserId, r.getUserId()).last("LIMIT 1"));
        if (visitor != null) {
            Visitor v = new Visitor();
            v.setId(visitor.getId());
            v.setVisitCount(visitor.getVisitCount() + 1);
            v.setPoints(visitor.getPoints() + 10);
            visitorMapper.updateById(v);
        }
    }

    /** 预约单号：R + yyyyMMddHHmmss + 4位随机数 */
    private String generateNo() {
        return "R" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
