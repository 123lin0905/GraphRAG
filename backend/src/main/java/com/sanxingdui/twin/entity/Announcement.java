package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 公告表（announcement）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("announcement")
@Schema(description = "公告表")
public class Announcement extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 公告标题 */
    @Schema(description = "公告标题")
    private String title;

    /** 公告内容 */
    @Schema(description = "公告内容")
    private String content;

    /** 公告类型：NOTICE-通知 NEWS-新闻 CLOSE-闭馆通知 */
    @Schema(description = "公告类型：NOTICE-通知 NEWS-新闻 CLOSE-闭馆通知")
    private String annType;

    /** 是否置顶：0-否 1-是 */
    @Schema(description = "是否置顶：0-否 1-是")
    private Integer isTop;

    /** 发布人ID */
    @Schema(description = "发布人ID")
    private Long publisherId;

    /** 发布时间 */
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    /** 阅读量 */
    @Schema(description = "阅读量")
    private Integer viewCount;

    /** 状态：0-草稿 1-已发布 */
    @Schema(description = "状态：0-草稿 1-已发布")
    private Integer status;
}
