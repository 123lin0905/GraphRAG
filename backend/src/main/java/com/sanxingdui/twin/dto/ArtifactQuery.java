package com.sanxingdui.twin.dto;

import com.sanxingdui.twin.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文物分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文物分页查询条件")
public class ArtifactQuery extends PageQuery {

    @Schema(description = "分类ID（含子分类）")
    private Long categoryId;

    @Schema(description = "祭祀坑ID")
    private Long pitId;

    @Schema(description = "材质ID")
    private Long materialId;

    @Schema(description = "年代/文化期ID")
    private Long periodId;

    @Schema(description = "展厅ID")
    private Long hallId;

    @Schema(description = "是否镇馆之宝：0/1")
    private Integer isTreasure;

    @Schema(description = "状态：0-库房 1-在展 2-修复中")
    private Integer status;

    @Schema(description = "排序方式：hot-按热度 new-按最新 默认按编号")
    private String orderBy;
}
