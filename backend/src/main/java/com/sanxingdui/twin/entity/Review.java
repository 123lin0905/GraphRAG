package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评价表（review）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review")
@Schema(description = "评价表")
public class Review extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 评价用户ID */
    @Schema(description = "评价用户ID")
    private Long userId;

    /** 评价对象类型：ARTIFACT-文物 ROUTE-路线 MUSEUM-博物馆 QA-问答服务 */
    @Schema(description = "评价对象类型：ARTIFACT-文物 ROUTE-路线 MUSEUM-博物馆 QA-问答服务")
    private String targetType;

    /** 评价对象ID */
    @Schema(description = "评价对象ID")
    private Long targetId;

    /** 评分（1-5星） */
    @Schema(description = "评分（1-5星）")
    private Integer rating;

    /** 评价内容 */
    @Schema(description = "评价内容")
    private String content;

    /** 评价图片URL（逗号分隔） */
    @Schema(description = "评价图片URL（逗号分隔）")
    private String images;

    /** 点赞数 */
    @Schema(description = "点赞数")
    private Integer likeCount;

    /** 审核状态：0-待审核 1-已通过 2-已屏蔽 */
    @Schema(description = "审核状态：0-待审核 1-已通过 2-已屏蔽")
    private Integer status;

    /** 馆方回复 */
    @Schema(description = "馆方回复")
    private String reply;
}
