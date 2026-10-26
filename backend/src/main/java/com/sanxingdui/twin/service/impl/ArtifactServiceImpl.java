package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.common.ResultCode;
import com.sanxingdui.twin.dto.ArtifactQuery;
import com.sanxingdui.twin.entity.Artifact;
import com.sanxingdui.twin.entity.ArtifactImage;
import com.sanxingdui.twin.entity.ArtifactLiterature;
import com.sanxingdui.twin.entity.Favorite;
import com.sanxingdui.twin.entity.KgEntity;
import com.sanxingdui.twin.entity.Literature;
import com.sanxingdui.twin.entity.Model3d;
import com.sanxingdui.twin.entity.RestorationRecord;
import com.sanxingdui.twin.entity.Review;
import com.sanxingdui.twin.mapper.ArtifactImageMapper;
import com.sanxingdui.twin.mapper.ArtifactLiteratureMapper;
import com.sanxingdui.twin.mapper.ArtifactMapper;
import com.sanxingdui.twin.mapper.FavoriteMapper;
import com.sanxingdui.twin.mapper.KgEntityMapper;
import com.sanxingdui.twin.mapper.LiteratureMapper;
import com.sanxingdui.twin.mapper.Model3dMapper;
import com.sanxingdui.twin.mapper.RestorationRecordMapper;
import com.sanxingdui.twin.mapper.ReviewMapper;
import com.sanxingdui.twin.security.SecurityUtils;
import com.sanxingdui.twin.service.ArtifactService;
import com.sanxingdui.twin.service.KnowledgeGraphService;
import com.sanxingdui.twin.vo.ArtifactDetailVO;
import com.sanxingdui.twin.vo.ArtifactVO;
import com.sanxingdui.twin.vo.GraphVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文物服务实现
 */
@Service
@RequiredArgsConstructor
public class ArtifactServiceImpl extends ServiceImpl<ArtifactMapper, Artifact> implements ArtifactService {

    private final ArtifactImageMapper imageMapper;
    private final Model3dMapper model3dMapper;
    private final ArtifactLiteratureMapper artifactLiteratureMapper;
    private final LiteratureMapper literatureMapper;
    private final RestorationRecordMapper restorationMapper;
    private final ReviewMapper reviewMapper;
    private final FavoriteMapper favoriteMapper;
    private final KgEntityMapper kgEntityMapper;
    private final KnowledgeGraphService knowledgeGraphService;

    @Override
    public PageResult<ArtifactVO> pageVo(ArtifactQuery query) {
        Page<ArtifactVO> page = new Page<>(query.getCurrent(), query.getSize());
        return PageResult.of(baseMapper.selectVoPage(page, query));
    }

    @Override
    public ArtifactDetailVO detail(Long id, boolean countView) {
        if (countView) {
            baseMapper.increaseViewCount(id);
        }
        ArtifactVO artifact = baseMapper.selectVoById(id);
        if (artifact == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文物不存在");
        }
        ArtifactDetailVO vo = new ArtifactDetailVO();
        vo.setArtifact(artifact);

        // 图片：封面优先，再按排序号
        vo.setImages(imageMapper.selectList(new LambdaQueryWrapper<ArtifactImage>()
                .eq(ArtifactImage::getArtifactId, id)
                .orderByDesc(ArtifactImage::getIsCover).orderByAsc(ArtifactImage::getSort)));

        // 3DGS 模型：默认模型优先
        vo.setModels(model3dMapper.selectList(new LambdaQueryWrapper<Model3d>()
                .eq(Model3d::getArtifactId, id)
                .orderByDesc(Model3d::getIsDefault).orderByAsc(Model3d::getId)));

        // 知识图谱：找到文物对应实体，取一跳邻居
        KgEntity entity = kgEntityMapper.selectOne(new LambdaQueryWrapper<KgEntity>()
                .eq(KgEntity::getRefTable, "artifact").eq(KgEntity::getRefId, id).last("LIMIT 1"));
        vo.setGraph(entity == null ? new GraphVO() : knowledgeGraphService.neighbors(entity.getId(), 1, 60));

        // 相关文献
        List<Long> litIds = artifactLiteratureMapper.selectList(new LambdaQueryWrapper<ArtifactLiterature>()
                        .eq(ArtifactLiterature::getArtifactId, id))
                .stream().map(ArtifactLiterature::getLiteratureId).distinct().toList();
        vo.setLiterature(litIds.isEmpty() ? List.of() : literatureMapper.selectBatchIds(litIds));

        // 修复记录
        vo.setRestorations(restorationMapper.selectList(new LambdaQueryWrapper<RestorationRecord>()
                .eq(RestorationRecord::getArtifactId, id).orderByDesc(RestorationRecord::getId)));

        // 相关文物：同一祭祀坑的其他文物
        List<ArtifactVO> related = new ArrayList<>();
        if (artifact.getPitId() != null) {
            List<Long> ids = baseMapper.selectList(new LambdaQueryWrapper<Artifact>()
                            .select(Artifact::getId)
                            .eq(Artifact::getPitId, artifact.getPitId()).ne(Artifact::getId, id)
                            .orderByDesc(Artifact::getViewCount).last("LIMIT 6"))
                    .stream().map(Artifact::getId).toList();
            if (!ids.isEmpty()) {
                related = baseMapper.selectVoByIds(ids);
            }
        }
        vo.setRelatedArtifacts(related);

        // 评分统计（仅统计审核通过的评价）
        List<Map<String, Object>> stat = reviewMapper.selectMaps(new QueryWrapper<Review>()
                .select("COUNT(*) AS cnt", "IFNULL(AVG(rating), 0) AS avg_rating")
                .eq("target_type", "ARTIFACT").eq("target_id", id).eq("status", 1));
        Map<String, Object> s = stat.isEmpty() ? Map.of() : stat.get(0);
        vo.setReviewCount(s.get("cnt") == null ? 0L : ((Number) s.get("cnt")).longValue());
        vo.setAvgRating(s.get("avg_rating") == null ? 0.0
                : Math.round(((Number) s.get("avg_rating")).doubleValue() * 10) / 10.0);

        // 当前用户收藏状态
        Long userId = SecurityUtils.currentUserId();
        vo.setFavorited(userId != null && favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getTargetType, "ARTIFACT")
                .eq(Favorite::getTargetId, id)) > 0);
        return vo;
    }

    @Override
    public List<ArtifactVO> hot(int limit) {
        ArtifactQuery q = new ArtifactQuery();
        q.setOrderBy("hot");
        q.setStatus(1);
        q.setCurrent(1);
        q.setSize(Math.min(Math.max(limit, 1), 50));
        return pageVo(q).getRecords();
    }
}
