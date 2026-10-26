package com.sanxingdui.twin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.dto.FavoriteDTO;
import com.sanxingdui.twin.entity.Favorite;
import com.sanxingdui.twin.vo.ArtifactVO;

import java.util.List;

/**
 * 收藏服务
 */
public interface FavoriteService extends IService<Favorite> {

    /** 收藏/取消收藏切换，返回切换后是否已收藏 */
    boolean toggle(FavoriteDTO dto);

    /** 是否已收藏 */
    boolean isFavorited(String targetType, Long targetId);

    /** 我收藏的文物 */
    PageResult<ArtifactVO> myArtifacts(PageQuery query);

    /** 我的收藏记录（全部类型） */
    List<Favorite> myFavorites(String targetType);
}
