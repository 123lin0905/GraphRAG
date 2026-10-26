package com.sanxingdui.twin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sanxingdui.twin.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏表 Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
