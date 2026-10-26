package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.RestorationRecord;
import com.sanxingdui.twin.mapper.RestorationRecordMapper;
import com.sanxingdui.twin.service.RestorationRecordService;
import org.springframework.stereotype.Service;

/**
 * 文物修复记录表 服务实现
 */
@Service
public class RestorationRecordServiceImpl extends ServiceImpl<RestorationRecordMapper, RestorationRecord> implements RestorationRecordService {
}
