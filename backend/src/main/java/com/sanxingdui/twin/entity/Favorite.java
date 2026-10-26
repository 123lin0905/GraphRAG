package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏表（favorite）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("favorite")
@Schema(description = "收藏表")
public class Favorite extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Schema(description = "用户ID")
    private Long userId;

    /** 收藏对象类型：ARTIFACT-文物 ROUTE-路线 */
    @Schema(description = "收藏对象类型：ARTIFACT-文物 ROUTE-路线")
    private String targetType;

    /** 收藏对象ID */
    @Schema(description = "收藏对象ID")
    private Long targetId;
}
