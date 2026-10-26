package com.sanxingdui.twin.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一业务状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有访问权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "数据冲突"),
    BUSINESS_ERROR(500, "业务处理失败"),
    SERVER_ERROR(500, "服务器内部错误"),
    LLM_ERROR(502, "大模型服务调用失败");

    /** 状态码 */
    private final int code;
    /** 默认提示信息 */
    private final String message;
}
