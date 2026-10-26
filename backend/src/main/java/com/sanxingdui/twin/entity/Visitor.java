package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 游客档案表（visitor）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("visitor")
@Schema(description = "游客档案表")
public class Visitor extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 关联用户ID */
    @Schema(description = "关联用户ID")
    private Long userId;

    /** 真实姓名 */
    @Schema(description = "真实姓名")
    private String realName;

    /** 年龄段：少年/青年/中年/老年 */
    @Schema(description = "年龄段：少年/青年/中年/老年")
    private String ageGroup;

    /** 常住地区 */
    @Schema(description = "常住地区")
    private String region;

    /** 游客类型：INDIVIDUAL-散客 STUDENT-学生 TEAM-团队 RESEARCHER-研究者 */
    @Schema(description = "游客类型：INDIVIDUAL-散客 STUDENT-学生 TEAM-团队 RESEARCHER-研究者")
    private String visitorType;

    /** 兴趣标签（逗号分隔，用于路线推荐） */
    @Schema(description = "兴趣标签（逗号分隔，用于路线推荐）")
    private String interests;

    /** 累计到馆次数 */
    @Schema(description = "累计到馆次数")
    private Integer visitCount;

    /** 积分 */
    @Schema(description = "积分")
    private Integer points;
}
