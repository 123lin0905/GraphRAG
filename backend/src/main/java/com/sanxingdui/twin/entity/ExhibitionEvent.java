package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 展览活动表（exhibition_event）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exhibition_event")
@Schema(description = "展览活动表")
public class ExhibitionEvent extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 博物馆ID */
    @Schema(description = "博物馆ID")
    private Long museumId;

    /** 举办展厅ID */
    @Schema(description = "举办展厅ID")
    private Long hallId;

    /** 活动标题 */
    @Schema(description = "活动标题")
    private String title;

    /** 活动类型：EXHIBITION-展览 LECTURE-讲座 WORKSHOP-研学 ONLINE-线上活动 */
    @Schema(description = "活动类型：EXHIBITION-展览 LECTURE-讲座 WORKSHOP-研学 ONLINE-线上活动")
    private String eventType;

    /** 开始时间 */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /** 结束时间 */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /** 活动地点 */
    @Schema(description = "活动地点")
    private String location;

    /** 主办方 */
    @Schema(description = "主办方")
    private String organizer;

    /** 名额上限 */
    @Schema(description = "名额上限")
    private Integer capacity;

    /** 活动内容 */
    @Schema(description = "活动内容")
    private String content;

    /** 封面图URL */
    @Schema(description = "封面图URL")
    private String coverUrl;

    /** 状态：0-未开始 1-进行中 2-已结束 */
    @Schema(description = "状态：0-未开始 1-进行中 2-已结束")
    private Integer status;
}
