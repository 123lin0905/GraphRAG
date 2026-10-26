package com.sanxingdui.twin.vo;

import com.sanxingdui.twin.entity.Artifact;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文物列表视图对象：在文物实体基础上附带关联字典名称
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文物视图对象")
public class ArtifactVO extends Artifact {
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "年代/文化期名称")
    private String periodName;
    @Schema(description = "祭祀坑名称")
    private String pitName;
    @Schema(description = "祭祀坑编号")
    private String pitCode;
    @Schema(description = "材质名称")
    private String materialName;
    @Schema(description = "展厅名称")
    private String hallName;
    @Schema(description = "是否有可用3D模型")
    private Boolean hasModel;
}
