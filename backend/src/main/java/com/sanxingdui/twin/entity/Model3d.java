package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 三维模型表（3DGS）（model_3d）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("model_3d")
@Schema(description = "三维模型表（3DGS）")
public class Model3d extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 关联文物ID（展厅场景模型可为空） */
    @Schema(description = "关联文物ID（展厅场景模型可为空）")
    private Long artifactId;

    /** 关联展厅ID（场景级模型） */
    @Schema(description = "关联展厅ID（场景级模型）")
    private Long hallId;

    /** 模型名称 */
    @Schema(description = "模型名称")
    private String name;

    /** 模型类型：OBJECT-单体文物 SCENE-展厅场景 */
    @Schema(description = "模型类型：OBJECT-单体文物 SCENE-展厅场景")
    private String modelType;

    /** 模型文件URL */
    @Schema(description = "模型文件URL")
    private String modelUrl;

    /** 文件格式：ply/splat/ksplat/spz */
    @Schema(description = "文件格式：ply/splat/ksplat/spz")
    private String format;

    /** 文件大小（字节） */
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    /** 高斯基元数量 */
    @Schema(description = "高斯基元数量")
    private Integer gaussianCount;

    /** 球谐函数阶数（0-3） */
    @Schema(description = "球谐函数阶数（0-3）")
    private Integer shDegree;

    /** 训练用多视角图像数量 */
    @Schema(description = "训练用多视角图像数量")
    private Integer imageCount;

    /** 采集设备 */
    @Schema(description = "采集设备")
    private String captureDevice;

    /** 位姿估计工具（如 COLMAP） */
    @Schema(description = "位姿估计工具（如 COLMAP）")
    private String sfmTool;

    /** 训练框架/实现（如 gaussian-splatting官方实现、gsplat） */
    @Schema(description = "训练框架/实现（如 gaussian-splatting官方实现、gsplat）")
    private String trainedBy;

    /** 训练迭代次数 */
    @Schema(description = "训练迭代次数")
    private Integer trainIterations;

    /** 训练耗时（分钟） */
    @Schema(description = "训练耗时（分钟）")
    private BigDecimal trainMinutes;

    /** 峰值信噪比PSNR（dB） */
    @Schema(description = "峰值信噪比PSNR（dB）")
    private BigDecimal psnr;

    /** 结构相似性SSIM */
    @Schema(description = "结构相似性SSIM")
    private BigDecimal ssim;

    /** 感知相似度LPIPS（越低越好） */
    @Schema(description = "感知相似度LPIPS（越低越好）")
    private BigDecimal lpips;

    /** 默认相机位置（JSON数组 [x,y,z]） */
    @Schema(description = "默认相机位置（JSON数组 [x,y,z]）")
    private String cameraPosition;

    /** 默认相机观察点（JSON数组 [x,y,z]） */
    @Schema(description = "默认相机观察点（JSON数组 [x,y,z]）")
    private String cameraTarget;

    /** 缩略图URL */
    @Schema(description = "缩略图URL")
    private String thumbnailUrl;

    /** 是否该文物默认展示模型 */
    @Schema(description = "是否该文物默认展示模型")
    private Integer isDefault;

    /** 状态：0-训练中 1-可用 2-已下线 */
    @Schema(description = "状态：0-训练中 1-可用 2-已下线")
    private Integer status;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}
