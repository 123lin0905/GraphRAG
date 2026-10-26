package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.QaMessage;
import com.sanxingdui.twin.mapper.QaMessageMapper;
import com.sanxingdui.twin.service.QaMessageService;
import org.springframework.stereotype.Service;

/**
 * 问答消息表 服务实现
 */
@Service
public class QaMessageServiceImpl extends ServiceImpl<QaMessageMapper, QaMessage> implements QaMessageService {
}
