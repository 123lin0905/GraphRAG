package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 文物主表（artifact）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("artifact")
@Schema(description = "文物主表")
public class Artifact extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 文物编号（系统内编号） */
    @Schema(description = "文物编号（系统内编号）")
    private String artifactNo;

    /** 文物名称 */
    @Schema(description = "文物名称")
    private String name;

    /** 别名（逗号分隔） */
    @Schema(description = "别名（逗号分隔）")
    private String alias;

    /** 分类ID */
    @Schema(description = "分类ID")
    private Long categoryId;

    /** 年代/文化期ID */
    @Schema(description = "年代/文化期ID")
    private Long periodId;

    /** 出土祭祀坑ID */
    @Schema(description = "出土祭祀坑ID")
    private Long pitId;

    /** 主要材质ID */
    @Schema(description = "主要材质ID")
    private Long materialId;

    /** 陈列展厅ID */
    @Schema(description = "陈列展厅ID")
    private Long hallId;

    /** 陈列展柜ID */
    @Schema(description = "陈列展柜ID")
    private Long showcaseId;

    /** 尺寸描述（如 通高396厘米） */
    @Schema(description = "尺寸描述（如 通高396厘米）")
    private String dimensions;

    /** 高度（厘米，约数） */
    @Schema(description = "高度（厘米，约数）")
    private BigDecimal heightCm;

    /** 宽度（厘米，约数） */
    @Schema(description = "宽度（厘米，约数）")
    private BigDecimal widthCm;

    /** 重量（克，约数） */
    @Schema(description = "重量（克，约数）")
    private BigDecimal weightG;

    /** 出土年份 */
    @Schema(description = "出土年份")
    private Integer excavationYear;

    /** 文物等级：一级/二级/三级/一般 */
    @Schema(description = "文物等级：一级/二级/三级/一般")
    private String grade;

    /** 是否镇馆之宝：0-否 1-是 */
    @Schema(description = "是否镇馆之宝：0-否 1-是")
    private Integer isTreasure;

    /** 文物描述 */
    @Schema(description = "文物描述")
    private String description;

    /** 文化价值与研究意义 */
    @Schema(description = "文化价值与研究意义")
    private String culturalValue;

    /** 语音导览讲解词 */
    @Schema(description = "语音导览讲解词")
    private String guideText;

    /** 封面图URL */
    @Schema(description = "封面图URL")
    private String coverUrl;

    /** 浏览次数 */
    @Schema(description = "浏览次数")
    private Integer viewCount;

    /** 收藏次数 */
    @Schema(description = "收藏次数")
    private Integer favoriteCount;

    /** 状态：0-下展/库房 1-在展 2-修复中 */
    @Schema(description = "状态：0-下展/库房 1-在展 2-修复中")
    private Integer status;
}
