package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

/**
 * 每日访问统计表（visit_statistics）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("visit_statistics")
@Schema(description = "每日访问统计表")
public class VisitStatistics extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 统计日期 */
    @Schema(description = "统计日期")
    private LocalDate statDate;

    /** 线下入馆人数 */
    @Schema(description = "线下入馆人数")
    private Integer offlineVisitors;

    /** 线上数字展厅访问量（PV） */
    @Schema(description = "线上数字展厅访问量（PV）")
    private Integer onlineVisits;

    /** 3D模型浏览次数 */
    @Schema(description = "3D模型浏览次数")
    private Integer modelViews;

    /** 智能问答次数 */
    @Schema(description = "智能问答次数")
    private Integer qaCount;

    /** 预约单数 */
    @Schema(description = "预约单数")
    private Integer reservationCount;
}
