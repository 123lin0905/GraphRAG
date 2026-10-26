package com.sanxingdui.twin.vo;

import com.sanxingdui.twin.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评价视图对象（附带用户昵称与对象名称）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "评价视图对象")
public class ReviewVO extends Review {
    @Schema(description = "评价用户昵称")
    private String nickname;
    @Schema(description = "评价用户头像")
    private String avatar;
    @Schema(description = "评价对象名称")
    private String targetName;
}
