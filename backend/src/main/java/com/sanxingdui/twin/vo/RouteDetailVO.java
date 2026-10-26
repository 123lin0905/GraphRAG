package com.sanxingdui.twin.vo;

import com.sanxingdui.twin.entity.GuideRoute;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 导览路线详情（含有序节点）
 */
@Data
@Schema(description = "导览路线详情")
public class RouteDetailVO {
    @Schema(description = "路线信息")
    private GuideRoute route;
    @Schema(description = "路线节点（按顺序）")
    private List<RouteNodeVO> nodes;
    @Schema(description = "总停留时长（分钟）")
    private Integer totalStayMinutes;
    @Schema(description = "平均评分")
    private Double avgRating;
}
