package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文物图片表（artifact_image）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("artifact_image")
@Schema(description = "文物图片表")
public class ArtifactImage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 文物ID */
    @Schema(description = "文物ID")
    private Long artifactId;

    /** 图片URL */
    @Schema(description = "图片URL")
    private String imageUrl;

    /** 图片类型：PHOTO-实拍 DETAIL-细节 LINE-线图 TRAIN-3DGS训练视图 */
    @Schema(description = "图片类型：PHOTO-实拍 DETAIL-细节 LINE-线图 TRAIN-3DGS训练视图")
    private String imageType;

    /** 图片标题 */
    @Schema(description = "图片标题")
    private String title;

    /** 图片宽度（像素） */
    @Schema(description = "图片宽度（像素）")
    private Integer widthPx;

    /** 图片高度（像素） */
    @Schema(description = "图片高度（像素）")
    private Integer heightPx;

    /** 是否封面：0-否 1-是 */
    @Schema(description = "是否封面：0-否 1-是")
    private Integer isCover;

    /** 排序号 */
    @Schema(description = "排序号")
    private Integer sort;
}
