package com.sanxingdui.twin.service;

import com.sanxingdui.twin.dto.LoginDTO;
import com.sanxingdui.twin.dto.PasswordChangeDTO;
import com.sanxingdui.twin.dto.RegisterDTO;
import com.sanxingdui.twin.vo.LoginVO;
import com.sanxingdui.twin.vo.UserInfoVO;

/**
 * 认证服务：登录、注册、当前用户、修改密码
 */
public interface AuthService {

    LoginVO login(LoginDTO dto, String clientIp);

    UserInfoVO register(RegisterDTO dto);

    UserInfoVO currentUserInfo();

    void changePassword(PasswordChangeDTO dto);
}
