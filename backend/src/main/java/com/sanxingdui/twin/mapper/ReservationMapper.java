package com.sanxingdui.twin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sanxingdui.twin.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 参观预约表 Mapper
 */
@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
}
