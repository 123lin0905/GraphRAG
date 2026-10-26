package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.SysPermission;
import com.sanxingdui.twin.mapper.SysPermissionMapper;
import com.sanxingdui.twin.service.SysPermissionService;
import org.springframework.stereotype.Service;

/**
 * 权限表 服务实现
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {
}
