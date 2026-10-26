package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文物分类表（artifact_category）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("artifact_category")
@Schema(description = "文物分类表")
public class ArtifactCategory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 父分类ID，0表示顶级 */
    @Schema(description = "父分类ID，0表示顶级")
    private Long parentId;

    /** 分类名称 */
    @Schema(description = "分类名称")
    private String name;

    /** 分类编码 */
    @Schema(description = "分类编码")
    private String code;

    /** 分类说明 */
    @Schema(description = "分类说明")
    private String description;

    /** 排序号 */
    @Schema(description = "排序号")
    private Integer sort;
}
