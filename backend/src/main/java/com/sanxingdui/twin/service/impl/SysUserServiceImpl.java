package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.SysUser;
import com.sanxingdui.twin.mapper.SysUserMapper;
import com.sanxingdui.twin.service.SysUserService;
import org.springframework.stereotype.Service;

/**
 * 系统用户表 服务实现
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}
