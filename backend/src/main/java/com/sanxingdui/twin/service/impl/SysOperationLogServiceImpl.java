package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.SysOperationLog;
import com.sanxingdui.twin.mapper.SysOperationLogMapper;
import com.sanxingdui.twin.service.SysOperationLogService;
import org.springframework.stereotype.Service;

/**
 * 系统操作日志表 服务实现
 */
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements SysOperationLogService {
}
