package com.sanxingdui.twin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sanxingdui.twin.annotation.OperationLog;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.common.ResultCode;
import com.sanxingdui.twin.dto.RoleAssignDTO;
import com.sanxingdui.twin.entity.SysUser;
import com.sanxingdui.twin.entity.SysUserRole;
import com.sanxingdui.twin.mapper.SysUserMapper;
import com.sanxingdui.twin.service.SysUserRoleService;
import com.sanxingdui.twin.service.SysUserService;
import com.sanxingdui.twin.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口（仅管理员）
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final SysUserService userService;
    private final SysUserMapper userMapper;
    private final SysUserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "用户分页查询")
    @GetMapping("/page")
    public Result<PageResult<UserInfoVO>> page(@Valid PageQuery query,
                                               @RequestParam(required = false) String userType,
                                               @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<SysUser>()
                .and(StringUtils.hasText(query.getKeyword()), q -> q.like(SysUser::getUsername, query.getKeyword())
                        .or().like(SysUser::getNickname, query.getKeyword())
                        .or().like(SysUser::getPhone, query.getKeyword()))
                .eq(StringUtils.hasText(userType), SysUser::getUserType, userType)
                .eq(status != null, SysUser::getStatus, status)
                .orderByAsc(SysUser::getId);
        Page<SysUser> page = userService.page(query.toPage(), w);
        return Result.success(PageResult.of(page, u -> {
            UserInfoVO vo = new UserInfoVO();
            BeanUtils.copyProperties(u, vo);
            vo.setRoles(userMapper.selectRoleCodes(u.getId()));
            return vo;
        }));
    }

    @Operation(summary = "启用/禁用用户", description = "status：0-禁用 1-启用")
    @OperationLog(module = "用户管理", value = "修改用户状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser user = requireUser(id);
        if ("admin".equals(user.getUsername()) && status == 0) {
            throw new BusinessException("不能禁用内置管理员账号");
        }
        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus(status);
        userService.updateById(update);
        return Result.success();
    }

    @Operation(summary = "分配角色")
    @OperationLog(module = "用户管理", value = "分配角色")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody RoleAssignDTO dto) {
        requireUser(id);
        userRoleService.remove(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        dto.getRoleIds().forEach(roleId -> {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(id);
            ur.setRoleId(roleId);
            userRoleService.save(ur);
        });
        return Result.success();
    }

    @Operation(summary = "重置密码为 123456")
    @OperationLog(module = "用户管理", value = "重置密码")
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        requireUser(id);
        SysUser update = new SysUser();
        update.setId(id);
        update.setPassword(passwordEncoder.encode("123456"));
        userService.updateById(update);
        return Result.success();
    }

    private SysUser requireUser(Long id) {
        SysUser user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }
}
