package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.dto.QaAskDTO;
import com.sanxingdui.twin.dto.QaFeedbackDTO;
import com.sanxingdui.twin.entity.QaMessage;
import com.sanxingdui.twin.entity.QaSession;
import com.sanxingdui.twin.rag.RetrievalResult;
import com.sanxingdui.twin.service.QaService;
import com.sanxingdui.twin.util.IpUtils;
import com.sanxingdui.twin.vo.QaAnswerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * GraphRAG 智能问答接口（匿名可用，登录后会话归属到用户）
 */
@Tag(name = "智能问答(GraphRAG)", description = "基于知识图谱子图检索 + 大模型生成的文物问答")
@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QaController {

    private final QaService qaService;

    @Operation(summary = "提问（同步）", description = "实体链接 → 1~2跳子图检索 → 文物/文献片段 → 大模型生成；未配置API Key时返回离线兜底回答")
    @PostMapping("/ask")
    public Result<QaAnswerVO> ask(@Valid @RequestBody QaAskDTO dto, HttpServletRequest request) {
        return Result.success(qaService.ask(dto, IpUtils.getClientIp(request)));
    }

    @Operation(summary = "提问（SSE流式，POST）",
            description = "事件依次为 meta（引用实体/三元组/子图）→ 多个 delta（增量文本，JSON {content}）→ done（消息ID、耗时）；出错时推送 error")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody QaAskDTO dto, HttpServletRequest request) {
        return qaService.askStream(dto, IpUtils.getClientIp(request));
    }

    @Operation(summary = "提问（SSE流式，GET，便于浏览器 EventSource 直接调用）", description = "登录用户可通过 token 查询参数传递令牌")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGet(@RequestParam String question, @RequestParam(required = false) Long sessionId,
                                HttpServletRequest request) {
        QaAskDTO dto = new QaAskDTO();
        dto.setQuestion(question);
        dto.setSessionId(sessionId);
        return qaService.askStream(dto, IpUtils.getClientIp(request));
    }

    @Operation(summary = "仅检索（调试 GraphRAG）", description = "返回实体链接结果、子图、三元组、文物与文献片段，不调用大模型")
    @GetMapping("/retrieve")
    public Result<RetrievalResult> retrieve(@RequestParam String question) {
        return Result.success(qaService.retrieveOnly(question));
    }

    @Operation(summary = "我的会话列表（需登录）")
    @GetMapping("/sessions")
    public Result<List<QaSession>> sessions() {
        return Result.success(qaService.mySessions());
    }

    @Operation(summary = "会话消息记录")
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<QaMessage>> messages(@PathVariable Long sessionId) {
        return Result.success(qaService.messages(sessionId));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        qaService.deleteSession(sessionId);
        return Result.success();
    }

    @Operation(summary = "回答反馈（有用/无用）")
    @PostMapping("/messages/{messageId}/feedback")
    public Result<Void> feedback(@PathVariable Long messageId, @Valid @RequestBody QaFeedbackDTO dto) {
        qaService.feedback(messageId, dto.getFeedback());
        return Result.success();
    }

    @Operation(summary = "大模型配置状态")
    @GetMapping("/llm-status")
    public Result<Map<String, Object>> llmStatus() {
        return Result.success(qaService.llmStatus());
    }

    @Operation(summary = "管理端：问答消息分页", description = "role=user/assistant，fallback=0/1")
    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @GetMapping("/admin/messages")
    public Result<PageResult<QaMessage>> adminMessages(@Valid PageQuery query,
                                                       @RequestParam(required = false) String role,
                                                       @RequestParam(required = false) Integer fallback) {
        return Result.success(qaService.pageMessages(query, role, fallback));
    }
}
