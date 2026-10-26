package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.GuideRouteNode;
import com.sanxingdui.twin.mapper.GuideRouteNodeMapper;
import com.sanxingdui.twin.service.GuideRouteNodeService;
import org.springframework.stereotype.Service;

/**
 * 导览路线节点表 服务实现
 */
@Service
public class GuideRouteNodeServiceImpl extends ServiceImpl<GuideRouteNodeMapper, GuideRouteNode> implements GuideRouteNodeService {
}
