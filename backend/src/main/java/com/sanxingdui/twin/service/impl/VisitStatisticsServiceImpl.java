package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.VisitStatistics;
import com.sanxingdui.twin.mapper.VisitStatisticsMapper;
import com.sanxingdui.twin.service.VisitStatisticsService;
import org.springframework.stereotype.Service;

/**
 * 每日访问统计表 服务实现
 */
@Service
public class VisitStatisticsServiceImpl extends ServiceImpl<VisitStatisticsMapper, VisitStatistics> implements VisitStatisticsService {
}
