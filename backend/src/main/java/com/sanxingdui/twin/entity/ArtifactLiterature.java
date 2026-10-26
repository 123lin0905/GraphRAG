package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文物文献关联表（artifact_literature）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("artifact_literature")
@Schema(description = "文物文献关联表")
public class ArtifactLiterature extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 文物ID */
    @Schema(description = "文物ID")
    private Long artifactId;

    /** 文献ID */
    @Schema(description = "文献ID")
    private Long literatureId;

    /** 引用说明 */
    @Schema(description = "引用说明")
    private String note;
}
