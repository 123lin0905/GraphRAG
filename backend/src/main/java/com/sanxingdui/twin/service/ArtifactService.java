package com.sanxingdui.twin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.dto.ArtifactQuery;
import com.sanxingdui.twin.entity.Artifact;
import com.sanxingdui.twin.vo.ArtifactDetailVO;
import com.sanxingdui.twin.vo.ArtifactVO;

import java.util.List;

/**
 * 文物服务接口
 */
public interface ArtifactService extends IService<Artifact> {

    /** 多条件分页查询（含关联字典名称） */
    PageResult<ArtifactVO> pageVo(ArtifactQuery query);

    /** 文物详情：图片、3DGS模型、知识图谱邻居、文献、修复记录、评分等 */
    ArtifactDetailVO detail(Long id, boolean countView);

    /** 热门文物（按浏览量） */
    List<ArtifactVO> hot(int limit);
}
