package com.sanxingdui.twin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sanxingdui.twin.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统用户 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 查询用户的角色编码 */
    @Select("SELECT r.role_code FROM sys_role r JOIN sys_user_role ur ON ur.role_id = r.id "
            + "WHERE ur.user_id = #{userId} AND ur.deleted = 0 AND r.deleted = 0 AND r.status = 1")
    List<String> selectRoleCodes(@Param("userId") Long userId);

    /** 查询用户的权限标识（通过角色聚合） */
    @Select("SELECT DISTINCT p.perm_code FROM sys_permission p "
            + "JOIN sys_role_permission rp ON rp.permission_id = p.id AND rp.deleted = 0 "
            + "JOIN sys_user_role ur ON ur.role_id = rp.role_id AND ur.deleted = 0 "
            + "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<String> selectPermissionCodes(@Param("userId") Long userId);
}
