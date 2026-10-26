package com.sanxingdui.twin.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.common.ResultCode;
import com.sanxingdui.twin.config.LlmProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * OpenAI 兼容的 Chat Completions 客户端（DeepSeek / 通义千问兼容模式 / Moonshot / OpenAI 等均可）。
 * 基于 JDK 自带 HttpClient，支持普通调用与 SSE 流式调用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmClient {

    private final ObjectMapper objectMapper;
    private final LlmProperties properties;

    /** 对话消息 */
    public record ChatMessage(String role, String content) {
    }

    /** 调用结果 */
    @Data
    public static class ChatResult {
        private String content;
        private String model;
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }

    private HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()))
                .build();
    }

    /**
     * 非流式调用
     */
    public ChatResult chat(LlmSettings settings, List<ChatMessage> messages) {
        try {
            HttpRequest request = buildRequest(settings, messages, false);
            HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new BusinessException(ResultCode.LLM_ERROR,
                        "大模型接口返回 " + response.statusCode() + "：" + abbreviate(response.body()));
            }
            JsonNode root = objectMapper.readTree(response.body());
            ChatResult result = new ChatResult();
            result.setContent(root.path("choices").path(0).path("message").path("content").asText(""));
            result.setModel(root.path("model").asText(settings.model()));
            fillUsage(result, root.path("usage"));
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ResultCode.LLM_ERROR, "大模型调用被中断");
        } catch (Exception e) {
            throw new BusinessException(ResultCode.LLM_ERROR, "大模型调用失败：" + e.getMessage());
        }
    }

    /**
     * 流式调用：逐段回调增量内容，结束后返回完整结果
     *
     * @param onDelta 增量文本回调
     */
    public ChatResult chatStream(LlmSettings settings, List<ChatMessage> messages, Consumer<String> onDelta) {
        ChatResult result = new ChatResult();
        result.setModel(settings.model());
        StringBuilder full = new StringBuilder();
        try {
            HttpRequest request = buildRequest(settings, messages, true);
            HttpResponse<InputStream> response = httpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new BusinessException(ResultCode.LLM_ERROR, "大模型接口返回 " + response.statusCode() + "：" + abbreviate(body));
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // SSE 协议：每行以 "data:" 开头，"[DONE]" 表示结束
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    JsonNode node = objectMapper.readTree(data);
                    JsonNode delta = node.path("choices").path(0).path("delta").path("content");
                    if (!delta.isMissingNode() && !delta.isNull() && !delta.asText().isEmpty()) {
                        full.append(delta.asText());
                        onDelta.accept(delta.asText());
                    }
                    if (node.hasNonNull("model")) {
                        result.setModel(node.get("model").asText());
                    }
                    if (node.hasNonNull("usage")) {
                        fillUsage(result, node.get("usage"));
                    }
                }
            }
            result.setContent(full.toString());
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ResultCode.LLM_ERROR, "大模型调用被中断");
        } catch (Exception e) {
            throw new BusinessException(ResultCode.LLM_ERROR, "大模型流式调用失败：" + e.getMessage());
        }
    }

    private HttpRequest buildRequest(LlmSettings settings, List<ChatMessage> messages, boolean stream) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", settings.model());
        body.put("temperature", settings.temperature());
        body.put("max_tokens", settings.maxTokens());
        body.put("stream", stream);
        if (stream) {
            // 要求在最后一个分片中返回 token 用量（DeepSeek/OpenAI 均支持）
            body.putObject("stream_options").put("include_usage", true);
        }
        ArrayNode arr = body.putArray("messages");
        for (ChatMessage m : messages) {
            arr.addObject().put("role", m.role()).put("content", m.content());
        }
        return HttpRequest.newBuilder(URI.create(settings.chatUrl()))
                .timeout(Duration.ofSeconds(properties.getReadTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + settings.apiKey())
                .header("Accept", stream ? "text/event-stream" : "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                .build();
    }

    private void fillUsage(ChatResult result, JsonNode usage) {
        if (usage == null || usage.isMissingNode() || usage.isNull()) {
            return;
        }
        result.setPromptTokens(usage.path("prompt_tokens").asInt());
        result.setCompletionTokens(usage.path("completion_tokens").asInt());
        result.setTotalTokens(usage.path("total_tokens").asInt());
    }

    private String abbreviate(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }
}
