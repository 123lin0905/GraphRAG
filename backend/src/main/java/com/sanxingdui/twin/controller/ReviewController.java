package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.annotation.OperationLog;
import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.dto.ReviewDTO;
import com.sanxingdui.twin.entity.Review;
import com.sanxingdui.twin.service.ReviewService;
import com.sanxingdui.twin.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 评价接口
 */
@Tag(name = "评价管理")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "发表评价（需登录）")
    @OperationLog(module = "评价", value = "发表评价")
    @PostMapping
    public Result<Review> publish(@Valid @RequestBody ReviewDTO dto) {
        return Result.success("评价成功", reviewService.publish(dto));
    }

    @Operation(summary = "某对象的评价列表（公开）", description = "targetType：ARTIFACT/ROUTE/MUSEUM/QA")
    @GetMapping("/target")
    public Result<PageResult<ReviewVO>> byTarget(@RequestParam String targetType, @RequestParam Long targetId,
                                                 @Valid PageQuery query) {
        return Result.success(reviewService.listByTarget(targetType, targetId, query));
    }

    @Operation(summary = "评分汇总（公开）")
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary(@RequestParam String targetType, @RequestParam Long targetId) {
        return Result.success(reviewService.summary(targetType, targetId));
    }

    @Operation(summary = "我的评价")
    @GetMapping("/my")
    public Result<PageResult<ReviewVO>> my(@Valid PageQuery query) {
        return Result.success(reviewService.myReviews(query));
    }

    @Operation(summary = "点赞评价")
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        reviewService.like(id);
        return Result.success();
    }

    @Operation(summary = "删除评价（本人或管理员）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return Result.success();
    }

    @Operation(summary = "管理端：评价分页")
    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @GetMapping("/admin/page")
    public Result<PageResult<ReviewVO>> adminPage(@Valid PageQuery query,
                                                  @RequestParam(required = false) String targetType,
                                                  @RequestParam(required = false) Integer status) {
        return Result.success(reviewService.adminPage(query, targetType, status));
    }

    @Operation(summary = "管理端：审核/屏蔽并回复", description = "status：1-通过 2-屏蔽")
    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @OperationLog(module = "评价", value = "审核评价")
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam Integer status,
                              @RequestParam(required = false) String reply) {
        reviewService.audit(id, status, reply);
        return Result.success();
    }
}
