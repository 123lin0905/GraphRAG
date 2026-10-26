package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.QaSession;
import com.sanxingdui.twin.mapper.QaSessionMapper;
import com.sanxingdui.twin.service.QaSessionService;
import org.springframework.stereotype.Service;

/**
 * 问答会话表 服务实现
 */
@Service
public class QaSessionServiceImpl extends ServiceImpl<QaSessionMapper, QaSession> implements QaSessionService {
}
