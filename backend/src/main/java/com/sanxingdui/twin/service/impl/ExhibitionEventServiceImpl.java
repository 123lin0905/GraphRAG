package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.ExhibitionEvent;
import com.sanxingdui.twin.mapper.ExhibitionEventMapper;
import com.sanxingdui.twin.service.ExhibitionEventService;
import org.springframework.stereotype.Service;

/**
 * 展览活动表 服务实现
 */
@Service
public class ExhibitionEventServiceImpl extends ServiceImpl<ExhibitionEventMapper, ExhibitionEvent> implements ExhibitionEventService {
}
