package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导览路线节点表（guide_route_node）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("guide_route_node")
@Schema(description = "导览路线节点表")
public class GuideRouteNode extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 路线ID */
    @Schema(description = "路线ID")
    private Long routeId;

    /** 节点顺序（从1开始） */
    @Schema(description = "节点顺序（从1开始）")
    private Integer seq;

    /** 节点类型：ARTIFACT-文物 HALL-展厅 POI-服务点 */
    @Schema(description = "节点类型：ARTIFACT-文物 HALL-展厅 POI-服务点")
    private String nodeType;

    /** 文物ID（文物节点） */
    @Schema(description = "文物ID（文物节点）")
    private Long artifactId;

    /** 展厅ID */
    @Schema(description = "展厅ID")
    private Long hallId;

    /** 节点标题 */
    @Schema(description = "节点标题")
    private String title;

    /** 节点讲解词 */
    @Schema(description = "节点讲解词")
    private String narration;

    /** 建议停留时长（分钟） */
    @Schema(description = "建议停留时长（分钟）")
    private Integer stayMinutes;

    /** 3D漫游相机位置（JSON [x,y,z]） */
    @Schema(description = "3D漫游相机位置（JSON [x,y,z]）")
    private String cameraPosition;

    /** 3D漫游相机观察点（JSON [x,y,z]） */
    @Schema(description = "3D漫游相机观察点（JSON [x,y,z]）")
    private String cameraTarget;
}
