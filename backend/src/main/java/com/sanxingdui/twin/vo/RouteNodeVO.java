package com.sanxingdui.twin.vo;

import com.sanxingdui.twin.entity.GuideRouteNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 路线节点视图对象（附带文物/展厅信息，供前端渲染与3D漫游定位）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "路线节点")
public class RouteNodeVO extends GuideRouteNode {
    @Schema(description = "文物封面")
    private String artifactCover;
    @Schema(description = "文物所在展厅名称")
    private String hallName;
    @Schema(description = "文物默认3DGS模型URL")
    private String modelUrl;
}
