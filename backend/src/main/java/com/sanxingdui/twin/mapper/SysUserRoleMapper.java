package com.sanxingdui.twin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sanxingdui.twin.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联表 Mapper
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
