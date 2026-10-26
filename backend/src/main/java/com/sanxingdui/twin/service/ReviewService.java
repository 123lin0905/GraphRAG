package com.sanxingdui.twin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.dto.ReviewDTO;
import com.sanxingdui.twin.entity.Review;
import com.sanxingdui.twin.vo.ReviewVO;

import java.util.Map;

/**
 * 评价服务
 */
public interface ReviewService extends IService<Review> {

    /** 发表评价 */
    Review publish(ReviewDTO dto);

    /** 某对象的评价列表（仅审核通过） */
    PageResult<ReviewVO> listByTarget(String targetType, Long targetId, PageQuery query);

    /** 评分汇总：平均分、数量、各星级数量 */
    Map<String, Object> summary(String targetType, Long targetId);

    /** 我的评价 */
    PageResult<ReviewVO> myReviews(PageQuery query);

    /** 删除评价（本人或管理员） */
    void deleteReview(Long id);

    /** 管理端分页 */
    PageResult<ReviewVO> adminPage(PageQuery query, String targetType, Integer status);

    /** 审核/屏蔽及回复 */
    void audit(Long id, Integer status, String reply);

    /** 点赞 */
    void like(Long id);
}
