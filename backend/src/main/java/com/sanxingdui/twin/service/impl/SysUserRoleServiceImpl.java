package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.SysUserRole;
import com.sanxingdui.twin.mapper.SysUserRoleMapper;
import com.sanxingdui.twin.service.SysUserRoleService;
import org.springframework.stereotype.Service;

/**
 * 用户角色关联表 服务实现
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {
}
