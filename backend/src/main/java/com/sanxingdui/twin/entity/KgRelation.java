package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 知识图谱关系表（kg_relation）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kg_relation")
@Schema(description = "知识图谱关系表")
public class KgRelation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 头实体ID */
    @Schema(description = "头实体ID")
    private Long sourceId;

    /** 尾实体ID */
    @Schema(description = "尾实体ID")
    private Long targetId;

    /** 关系类型编码（如 EXCAVATED_FROM） */
    @Schema(description = "关系类型编码（如 EXCAVATED_FROM）")
    private String relationType;

    /** 关系名称（如 出土于） */
    @Schema(description = "关系名称（如 出土于）")
    private String relationName;

    /** 关系权重 */
    @Schema(description = "关系权重")
    private BigDecimal weight;

    /** 关系说明/证据 */
    @Schema(description = "关系说明/证据")
    private String description;
}
