package com.sanxingdui.twin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sanxingdui.twin.entity.VisitStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日访问统计表 Mapper
 */
@Mapper
public interface VisitStatisticsMapper extends BaseMapper<VisitStatistics> {
}
