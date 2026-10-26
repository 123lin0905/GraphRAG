package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.SysRolePermission;
import com.sanxingdui.twin.mapper.SysRolePermissionMapper;
import com.sanxingdui.twin.service.SysRolePermissionService;
import org.springframework.stereotype.Service;

/**
 * 角色权限关联表 服务实现
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements SysRolePermissionService {
}
