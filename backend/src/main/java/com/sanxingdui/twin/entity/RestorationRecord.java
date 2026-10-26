package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

/**
 * 文物修复记录表（restoration_record）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("restoration_record")
@Schema(description = "文物修复记录表")
public class RestorationRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 文物ID */
    @Schema(description = "文物ID")
    private Long artifactId;

    /** 修复人员/团队 */
    @Schema(description = "修复人员/团队")
    private String restorer;

    /** 开始日期 */
    @Schema(description = "开始日期")
    private LocalDate startDate;

    /** 结束日期 */
    @Schema(description = "结束日期")
    private LocalDate endDate;

    /** 修复前病害状况 */
    @Schema(description = "修复前病害状况")
    private String conditionBefore;

    /** 修复方法与工艺 */
    @Schema(description = "修复方法与工艺")
    private String method;

    /** 修复后状况 */
    @Schema(description = "修复后状况")
    private String conditionAfter;

    /** 状态：PLANNED-计划 ONGOING-进行中 DONE-已完成 */
    @Schema(description = "状态：PLANNED-计划 ONGOING-进行中 DONE-已完成")
    private String status;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}
