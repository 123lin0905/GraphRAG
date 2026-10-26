package com.sanxingdui.twin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sanxingdui.twin.dto.ArtifactQuery;
import com.sanxingdui.twin.entity.Artifact;
import com.sanxingdui.twin.vo.ArtifactVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 文物 Mapper（多表关联查询见 resources/mapper/ArtifactMapper.xml）
 */
@Mapper
public interface ArtifactMapper extends BaseMapper<Artifact> {

    /** 关联分类、年代、祭祀坑、材质、展厅的分页查询 */
    IPage<ArtifactVO> selectVoPage(IPage<ArtifactVO> page, @Param("q") ArtifactQuery query);

    /** 查询单个文物视图 */
    ArtifactVO selectVoById(@Param("id") Long id);

    /** 根据ID集合批量查询视图 */
    List<ArtifactVO> selectVoByIds(@Param("ids") List<Long> ids);

    /** 浏览量 +1（原子更新） */
    @Update("UPDATE artifact SET view_count = view_count + 1 WHERE id = #{id}")
    int increaseViewCount(@Param("id") Long id);

    /** 收藏数增减（原子更新，不小于0） */
    @Update("UPDATE artifact SET favorite_count = GREATEST(favorite_count + #{delta}, 0) WHERE id = #{id}")
    int changeFavoriteCount(@Param("id") Long id, @Param("delta") int delta);
}
