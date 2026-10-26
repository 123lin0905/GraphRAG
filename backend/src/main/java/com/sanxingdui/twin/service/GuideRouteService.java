package com.sanxingdui.twin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sanxingdui.twin.entity.GuideRoute;
import com.sanxingdui.twin.vo.RouteDetailVO;

import java.util.List;

/**
 * 导览路线服务
 */
public interface GuideRouteService extends IService<GuideRoute> {

    /** 路线详情（含有序节点及文物、模型信息） */
    RouteDetailVO detail(Long id);

    /** 根据兴趣标签与可用时长推荐路线 */
    List<GuideRoute> recommend(String interests, Integer minutes);

    /** 开始导览：使用次数 +1 */
    void start(Long id);
}
