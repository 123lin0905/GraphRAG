package com.sanxingdui.twin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据大屏总览指标
 */
@Data
@Schema(description = "数据大屏总览")
public class DashboardOverviewVO {
    @Schema(description = "文物总数")
    private Long artifactCount;
    @Schema(description = "镇馆之宝数")
    private Long treasureCount;
    @Schema(description = "祭祀坑数")
    private Long pitCount;
    @Schema(description = "可交互三维模型数（不含占位 3DGS 记录）")
    private Long modelCount;
    @Schema(description = "真实扫描模型数（第三方公开扫描）")
    private Long scanModelCount;
    @Schema(description = "AI 复原 / 程序化建模模型数（项目自制）")
    private Long aiModelCount;
    @Schema(description = "高斯基元总数（不含占位 3DGS 记录）")
    private Long gaussianTotal;
    @Schema(description = "知识图谱实体数")
    private Long entityCount;
    @Schema(description = "知识图谱关系数")
    private Long relationCount;
    @Schema(description = "文献数")
    private Long literatureCount;
    @Schema(description = "注册用户数")
    private Long userCount;
    @Schema(description = "问答会话数")
    private Long qaSessionCount;
    @Schema(description = "问答消息数（用户提问）")
    private Long qaQuestionCount;
    @Schema(description = "导览路线数")
    private Long routeCount;
    @Schema(description = "有效预约单数")
    private Long reservationCount;
    @Schema(description = "今日预约人数")
    private Long todayReservationVisitors;
    @Schema(description = "近30天线下客流")
    private Long visitors30d;
    @Schema(description = "平均评分")
    private Double avgRating;
}
