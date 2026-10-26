package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.annotation.OperationLog;
import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.entity.SystemConfig;
import com.sanxingdui.twin.rag.LlmClient;
import com.sanxingdui.twin.rag.LlmSettings;
import com.sanxingdui.twin.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统参数配置接口（仅管理员），含大模型 API 配置与连通性测试
 */
@Tag(name = "系统参数配置")
@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemConfigController {

    private final SystemConfigService configService;
    private final LlmClient llmClient;

    @Operation(summary = "配置列表（API Key 脱敏）", description = "group：LLM/RAG/RESERVATION/SYSTEM")
    @GetMapping
    public Result<List<SystemConfig>> list(@RequestParam(required = false) String group) {
        return Result.success(configService.listMasked(group));
    }

    @Operation(summary = "批量更新配置", description = "请求体为 {configKey: configValue}；脱敏值（含******）不会覆盖原值")
    @OperationLog(module = "系统配置", value = "更新系统配置")
    @PutMapping
    public Result<Void> update(@RequestBody Map<String, String> values) {
        configService.updateValues(values);
        return Result.success();
    }

    @Operation(summary = "测试大模型连通性")
    @PostMapping("/llm/test")
    public Result<Map<String, Object>> testLlm() {
        LlmSettings settings = configService.llmSettings();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("configured", settings.configured());
        map.put("model", settings.model());
        if (!settings.configured()) {
            map.put("message", "未配置 API Key，问答将使用离线兜底模式");
            return Result.success(map);
        }
        long start = System.currentTimeMillis();
        LlmClient.ChatResult r = llmClient.chat(settings, List.of(new LlmClient.ChatMessage("user", "请用一句话介绍三星堆。")));
        map.put("latencyMs", System.currentTimeMillis() - start);
        map.put("reply", r.getContent());
        map.put("totalTokens", r.getTotalTokens());
        return Result.success(map);
    }
}
