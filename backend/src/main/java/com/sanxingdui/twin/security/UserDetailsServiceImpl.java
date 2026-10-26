package com.sanxingdui.twin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sanxingdui.twin.entity.SysUser;
import com.sanxingdui.twin.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 按用户名加载用户及其角色、权限
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public LoginUser loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(), user.getNickname(), user.getStatus(),
                sysUserMapper.selectRoleCodes(user.getId()), sysUserMapper.selectPermissionCodes(user.getId()));
    }
}
