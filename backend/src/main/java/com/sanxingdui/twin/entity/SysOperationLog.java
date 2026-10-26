package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统操作日志表（sys_operation_log）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_operation_log")
@Schema(description = "系统操作日志表")
public class SysOperationLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 操作用户ID（匿名为空） */
    @Schema(description = "操作用户ID（匿名为空）")
    private Long userId;

    /** 操作用户名 */
    @Schema(description = "操作用户名")
    private String username;

    /** 业务模块 */
    @Schema(description = "业务模块")
    private String module;

    /** 操作描述 */
    @Schema(description = "操作描述")
    private String operation;

    /** 调用的Java方法 */
    @Schema(description = "调用的Java方法")
    private String method;

    /** 请求URI */
    @Schema(description = "请求URI")
    private String requestUri;

    /** HTTP方法 */
    @Schema(description = "HTTP方法")
    private String requestMethod;

    /** 请求参数（截断保存） */
    @Schema(description = "请求参数（截断保存）")
    private String requestParams;

    /** 客户端IP */
    @Schema(description = "客户端IP")
    private String ip;

    /** 执行结果：0-失败 1-成功 */
    @Schema(description = "执行结果：0-失败 1-成功")
    private Integer status;

    /** 异常信息 */
    @Schema(description = "异常信息")
    private String errorMsg;

    /** 耗时（毫秒） */
    @Schema(description = "耗时（毫秒）")
    private Long costMs;
}
