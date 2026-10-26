package com.sanxingdui.twin.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanxingdui.twin.annotation.OperationLog;
import com.sanxingdui.twin.entity.SysOperationLog;
import com.sanxingdui.twin.mapper.SysOperationLogMapper;
import com.sanxingdui.twin.security.LoginUser;
import com.sanxingdui.twin.security.SecurityUtils;
import com.sanxingdui.twin.util.IpUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * 操作日志切面：记录被 {@link OperationLog} 标注的方法调用（敏感字段脱敏、参数截断）
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final int MAX_PARAM_LENGTH = 2000;

    private final SysOperationLogMapper logMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint pjp, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        SysOperationLog record = new SysOperationLog();
        record.setModule(operationLog.module());
        record.setOperation(operationLog.value());
        record.setMethod(pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName());
        record.setRequestParams(serializeArgs(pjp.getArgs()));
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            record.setRequestUri(request.getRequestURI());
            record.setRequestMethod(request.getMethod());
            record.setIp(IpUtils.getClientIp(request));
        }
        try {
            Object result = pjp.proceed();
            record.setStatus(1);
            return result;
        } catch (Throwable e) {
            record.setStatus(0);
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            record.setErrorMsg(msg.length() > 1000 ? msg.substring(0, 1000) : msg);
            throw e;
        } finally {
            // 登录接口执行后才有用户信息，因此在 finally 中读取
            LoginUser user = SecurityUtils.currentUser();
            if (user != null) {
                record.setUserId(user.getUserId());
                record.setUsername(user.getUsername());
            }
            record.setCostMs(System.currentTimeMillis() - start);
            try {
                logMapper.insert(record);
            } catch (Exception ex) {
                log.warn("操作日志保存失败：{}", ex.getMessage());
            }
        }
    }

    /** 序列化方法参数，过滤请求/响应/文件对象，并对密码脱敏 */
    private String serializeArgs(Object[] args) {
        try {
            Object[] filtered = Arrays.stream(args)
                    .filter(a -> !(a instanceof ServletRequest) && !(a instanceof ServletResponse) && !(a instanceof MultipartFile))
                    .toArray();
            String json = objectMapper.writeValueAsString(filtered)
                    .replaceAll("(\"[^\"]*(?i:password|api_?key|secret|token|configValue)[^\"]*\"\\s*:\\s*)\"[^\"]*\"", "$1\"******\"");
            return json.length() > MAX_PARAM_LENGTH ? json.substring(0, MAX_PARAM_LENGTH) + "..." : json;
        } catch (Exception e) {
            return "[参数无法序列化]";
        }
    }
}
