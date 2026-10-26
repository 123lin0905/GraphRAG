package com.sanxingdui.twin.vo;

import com.sanxingdui.twin.entity.ArtifactImage;
import com.sanxingdui.twin.entity.Literature;
import com.sanxingdui.twin.entity.Model3d;
import com.sanxingdui.twin.entity.RestorationRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 文物详情：基本信息 + 图片 + 3DGS模型 + 知识图谱邻居 + 文献 + 修复记录 + 评价统计
 */
@Data
@Schema(description = "文物详情")
public class ArtifactDetailVO {
    @Schema(description = "文物基本信息")
    private ArtifactVO artifact;
    @Schema(description = "图片列表")
    private List<ArtifactImage> images;
    @Schema(description = "3DGS模型列表（默认模型在前）")
    private List<Model3d> models;
    @Schema(description = "知识图谱一跳邻居子图")
    private GraphVO graph;
    @Schema(description = "相关文献")
    private List<Literature> literature;
    @Schema(description = "修复记录")
    private List<RestorationRecord> restorations;
    @Schema(description = "相关文物（同坑/同主题）")
    private List<ArtifactVO> relatedArtifacts;
    @Schema(description = "平均评分")
    private Double avgRating;
    @Schema(description = "评价数")
    private Long reviewCount;
    @Schema(description = "当前用户是否已收藏")
    private Boolean favorited;
}
