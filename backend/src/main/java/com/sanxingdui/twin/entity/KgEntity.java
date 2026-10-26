package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 知识图谱实体表（kg_entity）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kg_entity")
@Schema(description = "知识图谱实体表")
public class KgEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 实体名称 */
    @Schema(description = "实体名称")
    private String name;

    /** 实体类型：ARTIFACT-文物 PIT-祭祀坑 MATERIAL-材质 PERIOD-年代 CULTURE-文化 THEME-主题 SITE-遗址 MUSEUM-博物馆 PERSON-人物 HALL-展厅 LITERATURE-文献 TECH-工艺 */
    @Schema(description = "实体类型：ARTIFACT-文物 PIT-祭祀坑 MATERIAL-材质 PERIOD-年代 CULTURE-文化 THEME-主题 SITE-遗址 MUSEUM-博物馆 PERSON-人物 HALL-展厅 LITERATURE-文献 TECH-工艺")
    private String entityType;

    /** 别名（逗号分隔，用于问句实体链接） */
    @Schema(description = "别名（逗号分隔，用于问句实体链接）")
    private String aliases;

    /** 实体描述 */
    @Schema(description = "实体描述")
    private String description;

    /** 关联业务表名（如 artifact） */
    @Schema(description = "关联业务表名（如 artifact）")
    private String refTable;

    /** 关联业务表主键 */
    @Schema(description = "关联业务表主键")
    private Long refId;

    /** 扩展属性（JSON） */
    @Schema(description = "扩展属性（JSON）")
    private String properties;

    /** 实体重要度（可视化节点大小） */
    @Schema(description = "实体重要度（可视化节点大小）")
    private BigDecimal weight;
}
