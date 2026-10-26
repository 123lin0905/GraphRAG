package com.sanxingdui.twin.service;

import com.sanxingdui.twin.vo.DashboardOverviewVO;
import com.sanxingdui.twin.vo.NameValueVO;
import com.sanxingdui.twin.vo.TrendVO;

import java.util.List;
import java.util.Map;

/**
 * 数据大屏统计服务
 */
public interface DashboardService {

    DashboardOverviewVO overview();

    /** 文物分布：dimension = category | topCategory | pit | material | period | hall */
    List<NameValueVO> artifactDistribution(String dimension);

    TrendVO visitTrend(int days);

    Map<String, Object> qaStatistics(int days);

    Map<String, Object> modelStatistics();

    Map<String, Object> serviceStatistics();
}
