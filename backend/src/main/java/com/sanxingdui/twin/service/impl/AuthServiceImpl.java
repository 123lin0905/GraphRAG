package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.common.ResultCode;
import com.sanxingdui.twin.dto.LoginDTO;
import com.sanxingdui.twin.dto.PasswordChangeDTO;
import com.sanxingdui.twin.dto.RegisterDTO;
import com.sanxingdui.twin.entity.SysRole;
import com.sanxingdui.twin.entity.SysUser;
import com.sanxingdui.twin.entity.SysUserRole;
import com.sanxingdui.twin.entity.Visitor;
import com.sanxingdui.twin.mapper.SysRoleMapper;
import com.sanxingdui.twin.mapper.SysUserMapper;
import com.sanxingdui.twin.mapper.SysUserRoleMapper;
import com.sanxingdui.twin.mapper.VisitorMapper;
import com.sanxingdui.twin.security.JwtUtil;
import com.sanxingdui.twin.security.LoginUser;
import com.sanxingdui.twin.security.SecurityUtils;
import com.sanxingdui.twin.service.AuthService;
import com.sanxingdui.twin.vo.LoginVO;
import com.sanxingdui.twin.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String VISITOR_ROLE = "VISITOR";

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final VisitorMapper visitorMapper;

    @Override
    public LoginVO login(LoginDTO dto, String clientIp) {
        // 交给 Spring Security 校验用户名密码（BCrypt），失败抛出 BadCredentialsException / DisabledException
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        LoginUser loginUser = (LoginUser) auth.getPrincipal();

        // 记录登录时间与IP
        SysUser update = new SysUser();
        update.setId(loginUser.getUserId());
        update.setLastLoginTime(LocalDateTime.now());
        update.setLastLoginIp(clientIp);
        userMapper.updateById(update);

        String token = jwtUtil.generateToken(loginUser.getUserId(), loginUser.getUsername(), loginUser.getRoles());
        return new LoginVO(token, "Bearer", jwtUtil.getExpireSeconds(), buildUserInfo(loginUser));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO register(RegisterDTO dto) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (exists > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已被注册");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        user.setPhone(StringUtils.hasText(dto.getPhone()) ? dto.getPhone() : null);
        user.setEmail(dto.getEmail());
        user.setGender(0);
        user.setUserType(VISITOR_ROLE);
        user.setStatus(1);
        userMapper.insert(user);

        // 绑定游客角色
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, VISITOR_ROLE));
        if (role != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(role.getId());
            userRoleMapper.insert(ur);
        }
        // 创建游客档案
        Visitor visitor = new Visitor();
        visitor.setUserId(user.getId());
        visitor.setVisitorType("INDIVIDUAL");
        visitor.setVisitCount(0);
        visitor.setPoints(0);
        visitorMapper.insert(visitor);

        log.info("新游客注册：{}", user.getUsername());
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoles(java.util.List.of(VISITOR_ROLE));
        return vo;
    }

    @Override
    public UserInfoVO currentUserInfo() {
        LoginUser loginUser = SecurityUtils.currentUser();
        if (loginUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return buildUserInfo(loginUser);
    }

    @Override
    public void changePassword(PasswordChangeDTO dto) {
        Long userId = SecurityUtils.requireUserId();
        SysUser user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "原密码错误");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(update);
    }

    private UserInfoVO buildUserInfo(LoginUser loginUser) {
        SysUser user = userMapper.selectById(loginUser.getUserId());
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoles(loginUser.getRoles());
        vo.setPermissions(loginUser.getPermissions());
        return vo;
    }
}
