package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.common.ResultCode;
import com.sanxingdui.twin.dto.ReviewDTO;
import com.sanxingdui.twin.entity.Artifact;
import com.sanxingdui.twin.entity.GuideRoute;
import com.sanxingdui.twin.entity.Museum;
import com.sanxingdui.twin.entity.Review;
import com.sanxingdui.twin.entity.SysUser;
import com.sanxingdui.twin.mapper.ArtifactMapper;
import com.sanxingdui.twin.mapper.GuideRouteMapper;
import com.sanxingdui.twin.mapper.MuseumMapper;
import com.sanxingdui.twin.mapper.ReviewMapper;
import com.sanxingdui.twin.mapper.SysUserMapper;
import com.sanxingdui.twin.security.SecurityUtils;
import com.sanxingdui.twin.service.ReviewService;
import com.sanxingdui.twin.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评价服务实现
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    private final SysUserMapper userMapper;
    private final ArtifactMapper artifactMapper;
    private final GuideRouteMapper routeMapper;
    private final MuseumMapper museumMapper;

    @Override
    public Review publish(ReviewDTO dto) {
        Long userId = SecurityUtils.requireUserId();
        if (resolveTargetName(dto.getTargetType(), dto.getTargetId()) == null && !"QA".equals(dto.getTargetType())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评价对象不存在");
        }
        long exists = count(new LambdaQueryWrapper<Review>().eq(Review::getUserId, userId)
                .eq(Review::getTargetType, dto.getTargetType()).eq(Review::getTargetId, dto.getTargetId()));
        if (exists > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "您已评价过该对象");
        }
        Review review = new Review();
        BeanUtils.copyProperties(dto, review);
        review.setUserId(userId);
        review.setLikeCount(0);
        // 演示环境默认直接通过审核，生产可改为 0（待审核）
        review.setStatus(1);
        save(review);
        return review;
    }

    @Override
    public PageResult<ReviewVO> listByTarget(String targetType, Long targetId, PageQuery query) {
        Page<Review> page = page(query.toPage(), new LambdaQueryWrapper<Review>()
                .eq(Review::getTargetType, targetType).eq(Review::getTargetId, targetId).eq(Review::getStatus, 1)
                .orderByDesc(Review::getLikeCount).orderByDesc(Review::getId));
        return toVoPage(page);
    }

    @Override
    public Map<String, Object> summary(String targetType, Long targetId) {
        List<Map<String, Object>> rows = baseMapper.selectMaps(new QueryWrapper<Review>()
                .select("rating", "COUNT(*) AS cnt")
                .eq("target_type", targetType).eq("target_id", targetId).eq("status", 1).groupBy("rating"));
        Map<String, Long> stars = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            stars.put(i + "星", 0L);
        }
        long total = 0;
        long sum = 0;
        for (Map<String, Object> r : rows) {
            int rating = ((Number) r.get("rating")).intValue();
            long cnt = ((Number) r.get("cnt")).longValue();
            stars.put(rating + "星", cnt);
            total += cnt;
            sum += rating * cnt;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("count", total);
        map.put("avgRating", total == 0 ? 0 : Math.round(sum * 10.0 / total) / 10.0);
        map.put("stars", stars);
        return map;
    }

    @Override
    public PageResult<ReviewVO> myReviews(PageQuery query) {
        Long userId = SecurityUtils.requireUserId();
        return toVoPage(page(query.toPage(), new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId).orderByDesc(Review::getId)));
    }

    @Override
    public void deleteReview(Long id) {
        Review review = getById(id);
        if (review == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评价不存在");
        }
        if (!review.getUserId().equals(SecurityUtils.requireUserId()) && !SecurityUtils.isStaff()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己的评价");
        }
        removeById(id);
    }

    @Override
    public PageResult<ReviewVO> adminPage(PageQuery query, String targetType, Integer status) {
        return toVoPage(page(query.toPage(), new LambdaQueryWrapper<Review>()
                .like(StringUtils.hasText(query.getKeyword()), Review::getContent, query.getKeyword())
                .eq(StringUtils.hasText(targetType), Review::getTargetType, targetType)
                .eq(status != null, Review::getStatus, status)
                .orderByDesc(Review::getId)));
    }

    @Override
    public void audit(Long id, Integer status, String reply) {
        if (getById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评价不存在");
        }
        Review update = new Review();
        update.setId(id);
        update.setStatus(status);
        update.setReply(reply);
        updateById(update);
    }

    @Override
    public void like(Long id) {
        boolean ok = update(new LambdaUpdateWrapper<Review>().eq(Review::getId, id).setSql("like_count = like_count + 1"));
        if (!ok) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评价不存在");
        }
    }

    /** 批量填充用户昵称与对象名称，避免 N+1 查询用户表 */
    private PageResult<ReviewVO> toVoPage(Page<Review> page) {
        Set<Long> userIds = page.getRecords().stream().map(Review::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> users = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(SysUser::getId, Function.identity()));
        return PageResult.of(page, r -> {
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(r, vo);
            SysUser u = users.get(r.getUserId());
            if (u != null) {
                vo.setNickname(u.getNickname());
                vo.setAvatar(u.getAvatar());
            }
            vo.setTargetName(resolveTargetName(r.getTargetType(), r.getTargetId()));
            return vo;
        });
    }

    private String resolveTargetName(String type, Long id) {
        return switch (type) {
            case "ARTIFACT" -> {
                Artifact a = artifactMapper.selectById(id);
                yield a == null ? null : a.getName();
            }
            case "ROUTE" -> {
                GuideRoute r = routeMapper.selectById(id);
                yield r == null ? null : r.getName();
            }
            case "MUSEUM" -> {
                Museum m = museumMapper.selectById(id);
                yield m == null ? null : m.getName();
            }
            case "QA" -> "智能问答服务";
            default -> null;
        };
    }
}
