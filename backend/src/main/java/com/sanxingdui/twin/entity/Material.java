package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 材质表（material）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("material")
@Schema(description = "材质表")
public class Material extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 材质名称 */
    @Schema(description = "材质名称")
    private String name;

    /** 材质大类：金属/玉石/有机质/陶土 */
    @Schema(description = "材质大类：金属/玉石/有机质/陶土")
    private String category;

    /** 材质说明 */
    @Schema(description = "材质说明")
    private String description;

    /** 保存保护要点 */
    @Schema(description = "保存保护要点")
    private String conservationNote;
}
