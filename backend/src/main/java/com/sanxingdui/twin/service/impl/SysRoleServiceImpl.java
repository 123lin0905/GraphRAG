package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.SysRole;
import com.sanxingdui.twin.mapper.SysRoleMapper;
import com.sanxingdui.twin.service.SysRoleService;
import org.springframework.stereotype.Service;

/**
 * 角色表 服务实现
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
}
