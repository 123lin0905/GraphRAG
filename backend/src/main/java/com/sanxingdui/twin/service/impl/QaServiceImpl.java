package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.common.ResultCode;
import com.sanxingdui.twin.config.RagProperties;
import com.sanxingdui.twin.dto.QaAskDTO;
import com.sanxingdui.twin.entity.QaMessage;
import com.sanxingdui.twin.entity.QaSession;
import com.sanxingdui.twin.mapper.QaMessageMapper;
import com.sanxingdui.twin.mapper.QaSessionMapper;
import com.sanxingdui.twin.rag.GraphRagRetriever;
import com.sanxingdui.twin.rag.LlmClient;
import com.sanxingdui.twin.rag.LlmSettings;
import com.sanxingdui.twin.rag.RetrievalResult;
import com.sanxingdui.twin.security.SecurityUtils;
import com.sanxingdui.twin.service.QaService;
import com.sanxingdui.twin.service.SystemConfigService;
import com.sanxingdui.twin.vo.QaAnswerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GraphRAG 智能问答服务实现
 */
@Slf4j
@Service
public class QaServiceImpl implements QaService {

    /** 系统提示词：约束大模型只依据检索到的知识回答 */
    private static final String SYSTEM_PROMPT = """
            你是“三星堆文物数字孪生与智能导览系统”的AI导览员，名字叫“小堆”，面向博物馆观众讲解三星堆遗址与古蜀文明。
            回答要求：
            1. 优先依据【检索到的知识】回答，可结合常识做必要的衔接，但不得编造年代、尺寸、重量、出土单位等具体数据；
            2. 资料中没有的信息，请明确说明“现有资料未记载”，并建议观众关注馆方发布的权威信息；
            3. 学术上尚无定论的观点（如器物用途、象征含义），请使用“有学者认为”“一般认为”等表述；
            4. 使用通俗、生动、准确的中文，可适当分点，篇幅控制在350字以内；
            5. 回答末尾另起一行，以“参考：”列出所依据的文物、实体或文献名称。
            """;

    /** 离线兜底流式输出时每段字符数 */
    private static final int FALLBACK_CHUNK = 16;

    private final GraphRagRetriever retriever;
    private final LlmClient llmClient;
    private final SystemConfigService configService;
    private final QaSessionMapper sessionMapper;
    private final QaMessageMapper messageMapper;
    private final ObjectMapper objectMapper;
    private final RagProperties ragProperties;
    private final ThreadPoolTaskExecutor qaExecutor;

    public QaServiceImpl(GraphRagRetriever retriever, LlmClient llmClient, SystemConfigService configService,
                         QaSessionMapper sessionMapper, QaMessageMapper messageMapper, ObjectMapper objectMapper,
                         RagProperties ragProperties, @Qualifier("qaExecutor") ThreadPoolTaskExecutor qaExecutor) {
        this.retriever = retriever;
        this.llmClient = llmClient;
        this.configService = configService;
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.objectMapper = objectMapper;
        this.ragProperties = ragProperties;
        this.qaExecutor = qaExecutor;
    }

    // ===================================================================== 同步问答

    @Override
    public QaAnswerVO ask(QaAskDTO dto, String clientIp) {
        long start = System.currentTimeMillis();
        Long userId = SecurityUtils.currentUserId();
        QaSession session = getOrCreateSession(dto.getSessionId(), userId, dto.getQuestion(), clientIp);
        List<LlmClient.ChatMessage> history = loadHistory(session.getId());
        saveUserMessage(session.getId(), dto.getQuestion());

        RetrievalResult retrieval = retriever.retrieve(dto.getQuestion());
        LlmSettings settings = configService.llmSettings();

        String answer;
        boolean fallback = false;
        LlmClient.ChatResult chat = null;
        if (settings.configured()) {
            try {
                chat = llmClient.chat(settings, buildMessages(history, retrieval, dto.getQuestion()));
                answer = chat.getContent();
            } catch (BusinessException e) {
                log.warn("大模型调用失败，降级为离线回答：{}", e.getMessage());
                answer = buildFallbackAnswer(retrieval, "大模型服务暂不可用");
                fallback = true;
            }
        } else {
            answer = buildFallbackAnswer(retrieval, "当前未配置大模型 API Key");
            fallback = true;
        }
        long latency = System.currentTimeMillis() - start;
        QaMessage msg = saveAssistantMessage(session, answer, retrieval, fallback ? null : settings.model(), chat, latency, fallback);
        return toAnswerVO(session.getId(), msg, retrieval);
    }

    // ===================================================================== 流式问答

    @Override
    public SseEmitter askStream(QaAskDTO dto, String clientIp) {
        // SSE 最长保持 3 分钟
        SseEmitter emitter = new SseEmitter(180_000L);
        // 安全上下文是线程绑定的，需要在切换线程前取出当前用户
        Long userId = SecurityUtils.currentUserId();
        QaSession session = getOrCreateSession(dto.getSessionId(), userId, dto.getQuestion(), clientIp);

        qaExecutor.execute(() -> {
            long start = System.currentTimeMillis();
            try {
                List<LlmClient.ChatMessage> history = loadHistory(session.getId());
                saveUserMessage(session.getId(), dto.getQuestion());
                RetrievalResult retrieval = retriever.retrieve(dto.getQuestion());

                // 先推送检索元信息，前端可立即渲染引用实体与子图
                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("sessionId", session.getId());
                meta.put("citedEntities", retrieval.getSeedEntities());
                meta.put("triples", retrieval.getTriples());
                meta.put("references", retrieval.getLiteratureSnippets().stream().map(RetrievalResult.LiteratureSnippet::title).toList());
                meta.put("subgraph", retrieval.getSubgraph());
                send(emitter, "meta", meta);

                LlmSettings settings = configService.llmSettings();
                String answer;
                boolean fallback = false;
                LlmClient.ChatResult chat = null;
                if (settings.configured()) {
                    try {
                        chat = llmClient.chatStream(settings, buildMessages(history, retrieval, dto.getQuestion()),
                                delta -> send(emitter, "delta", Map.of("content", delta)));
                        answer = chat.getContent();
                    } catch (BusinessException e) {
                        log.warn("大模型流式调用失败，降级为离线回答：{}", e.getMessage());
                        answer = buildFallbackAnswer(retrieval, "大模型服务暂不可用");
                        fallback = true;
                        streamText(emitter, answer);
                    }
                } else {
                    answer = buildFallbackAnswer(retrieval, "当前未配置大模型 API Key");
                    fallback = true;
                    streamText(emitter, answer);
                }
                long latency = System.currentTimeMillis() - start;
                QaMessage msg = saveAssistantMessage(session, answer, retrieval, fallback ? null : settings.model(),
                        chat, latency, fallback);

                Map<String, Object> done = new LinkedHashMap<>();
                done.put("sessionId", session.getId());
                done.put("messageId", msg.getId());
                done.put("fallback", fallback);
                done.put("model", msg.getModelName());
                done.put("latencyMs", latency);
                done.put("totalTokens", msg.getTotalTokens());
                send(emitter, "done", done);
                emitter.complete();
            } catch (ClientAbortException e) {
                log.debug("客户端已断开 SSE 连接");
                emitter.complete();
            } catch (Exception e) {
                log.error("流式问答异常", e);
                try {
                    send(emitter, "error", Map.of("message", "问答服务异常：" + e.getMessage()));
                } catch (Exception ignored) {
                    // 连接可能已断开
                }
                emitter.complete();
            }
        });
        return emitter;
    }

    /** 离线兜底回答也按小段推送，保证前端“打字机”体验一致 */
    private void streamText(SseEmitter emitter, String text) {
        for (int i = 0; i < text.length(); i += FALLBACK_CHUNK) {
            send(emitter, "delta", Map.of("content", text.substring(i, Math.min(text.length(), i + FALLBACK_CHUNK))));
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void send(SseEmitter emitter, String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(objectMapper.writeValueAsString(data), MediaType.TEXT_PLAIN));
        } catch (IOException e) {
            throw new ClientAbortException(e);
        }
    }

    /** 客户端断开连接时抛出，用于终止流式生成 */
    private static class ClientAbortException extends RuntimeException {
        ClientAbortException(Throwable cause) {
            super(cause);
        }
    }

    // ===================================================================== 检索 & 提示词

    @Override
    public RetrievalResult retrieveOnly(String question) {
        return retriever.retrieve(question);
    }

    /** 组装发送给大模型的消息：系统提示 + 历史对话 + 带检索上下文的当前问题 */
    private List<LlmClient.ChatMessage> buildMessages(List<LlmClient.ChatMessage> history, RetrievalResult retrieval, String question) {
        List<LlmClient.ChatMessage> messages = new ArrayList<>();
        messages.add(new LlmClient.ChatMessage("system", SYSTEM_PROMPT));
        messages.addAll(history);
        String context = retrieval.isEmpty() ? "（知识库中未检索到与问题直接相关的内容）" : retrieval.toPromptContext();
        messages.add(new LlmClient.ChatMessage("user", "【检索到的知识】\n" + context + "\n【观众问题】\n" + question));
        return messages;
    }

    /**
     * 离线兜底回答：在未配置 API Key 或调用失败时，直接由检索结果组织答案，保证演示可用
     */
    private String buildFallbackAnswer(RetrievalResult r, String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("【离线模式】").append(reason).append("，以下内容由知识图谱检索结果自动整理：\n\n");
        if (r.isEmpty()) {
            sb.append("抱歉，知识库中暂未找到与问题相关的内容。您可以尝试询问具体文物（如青铜神树、金杖、青铜纵目面具）、"
                    + "祭祀坑（如二号坑、五号坑）或古蜀文化相关的问题。");
            return sb.toString();
        }
        if (!r.getArtifactSnippets().isEmpty()) {
            sb.append("**相关文物**\n");
            r.getArtifactSnippets().forEach(a -> sb.append("- ").append(a).append('\n'));
            sb.append('\n');
        }
        // 非文物实体的描述（文物描述已在上面给出）
        List<String> others = new ArrayList<>();
        r.getEntityDescriptions().forEach((name, desc) -> {
            if (!desc.startsWith("文物")) {
                others.add("- **" + name + "**（" + desc + "）");
            }
        });
        if (!others.isEmpty()) {
            sb.append("**相关概念**\n");
            others.forEach(o -> sb.append(o).append('\n'));
            sb.append('\n');
        }
        if (!r.getTriples().isEmpty()) {
            sb.append("**知识关联**\n");
            r.getTriples().stream().limit(10).forEach(t -> sb.append("- ").append(t).append('\n'));
            sb.append('\n');
        }
        if (!r.getLiteratureSnippets().isEmpty()) {
            sb.append("**文献摘录**\n");
            r.getLiteratureSnippets().forEach(l -> sb.append("- 《").append(l.title()).append("》：").append(l.snippet()).append('\n'));
            sb.append('\n');
        }
        sb.append("参考：").append(String.join("、", r.getSeedEntities()));
        return sb.toString();
    }

    // ===================================================================== 会话与消息持久化

    private QaSession getOrCreateSession(Long sessionId, Long userId, String question, String clientIp) {
        if (sessionId != null) {
            QaSession session = sessionMapper.selectById(sessionId);
            if (session == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
            }
            // 登录用户只能继续自己的会话；匿名会话允许匿名继续
            if (session.getUserId() != null && !session.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该会话");
            }
            return session;
        }
        QaSession session = new QaSession();
        session.setUserId(userId);
        session.setTitle(question.length() > 30 ? question.substring(0, 30) + "…" : question);
        session.setClientIp(clientIp);
        session.setMessageCount(0);
        session.setLastActiveTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    /** 读取最近若干轮历史对话，作为多轮问答上下文 */
    private List<LlmClient.ChatMessage> loadHistory(Long sessionId) {
        int limit = Math.max(0, ragProperties.getHistoryTurns()) * 2;
        if (limit == 0) {
            return List.of();
        }
        List<QaMessage> recent = messageMapper.selectList(new LambdaQueryWrapper<QaMessage>()
                .eq(QaMessage::getSessionId, sessionId).orderByDesc(QaMessage::getId).last("LIMIT " + limit));
        Collections.reverse(recent);
        return recent.stream().map(m -> new LlmClient.ChatMessage(m.getRole(), m.getContent())).toList();
    }

    private void saveUserMessage(Long sessionId, String question) {
        QaMessage msg = new QaMessage();
        msg.setSessionId(sessionId);
        msg.setRole("user");
        msg.setContent(question);
        msg.setIsFallback(0);
        messageMapper.insert(msg);
    }

    private QaMessage saveAssistantMessage(QaSession session, String answer, RetrievalResult retrieval, String model,
                                           LlmClient.ChatResult chat, long latency, boolean fallback) {
        QaMessage msg = new QaMessage();
        msg.setSessionId(session.getId());
        msg.setRole("assistant");
        msg.setContent(StringUtils.hasText(answer) ? answer : "（模型未返回内容）");
        msg.setRetrievedContext(toJson(Map.of(
                "linkMode", retrieval.getLinkMode(),
                "triples", retrieval.getTriples(),
                "artifacts", retrieval.getArtifactSnippets(),
                "literature", retrieval.getLiteratureSnippets())));
        msg.setCitedEntities(toJson(retrieval.getSeedEntities()));
        msg.setModelName(model);
        if (chat != null) {
            msg.setPromptTokens(chat.getPromptTokens());
            msg.setCompletionTokens(chat.getCompletionTokens());
            msg.setTotalTokens(chat.getTotalTokens());
        }
        msg.setLatencyMs(latency);
        msg.setIsFallback(fallback ? 1 : 0);
        messageMapper.insert(msg);

        QaSession update = new QaSession();
        update.setId(session.getId());
        update.setMessageCount((int) (long) messageMapper.selectCount(
                new LambdaQueryWrapper<QaMessage>().eq(QaMessage::getSessionId, session.getId())));
        update.setLastActiveTime(LocalDateTime.now());
        sessionMapper.updateById(update);
        return msg;
    }

    private QaAnswerVO toAnswerVO(Long sessionId, QaMessage msg, RetrievalResult retrieval) {
        QaAnswerVO vo = new QaAnswerVO();
        vo.setSessionId(sessionId);
        vo.setMessageId(msg.getId());
        vo.setAnswer(msg.getContent());
        vo.setFallback(msg.getIsFallback() == 1);
        vo.setModel(msg.getModelName());
        vo.setLatencyMs(msg.getLatencyMs());
        vo.setPromptTokens(msg.getPromptTokens());
        vo.setCompletionTokens(msg.getCompletionTokens());
        vo.setTotalTokens(msg.getTotalTokens());
        vo.setCitedEntities(retrieval.getSeedEntities());
        vo.setTriples(retrieval.getTriples());
        vo.setReferences(retrieval.getLiteratureSnippets().stream().map(RetrievalResult.LiteratureSnippet::title).toList());
        vo.setSubgraph(retrieval.getSubgraph());
        return vo;
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    // ===================================================================== 会话管理

    @Override
    public List<QaSession> mySessions() {
        Long userId = SecurityUtils.requireUserId();
        return sessionMapper.selectList(new LambdaQueryWrapper<QaSession>()
                .eq(QaSession::getUserId, userId).orderByDesc(QaSession::getLastActiveTime));
    }

    @Override
    public List<QaMessage> messages(Long sessionId) {
        checkSessionAccess(sessionId);
        return messageMapper.selectList(new LambdaQueryWrapper<QaMessage>()
                .eq(QaMessage::getSessionId, sessionId).orderByAsc(QaMessage::getId));
    }

    @Override
    public void deleteSession(Long sessionId) {
        checkSessionAccess(sessionId);
        messageMapper.delete(new LambdaQueryWrapper<QaMessage>().eq(QaMessage::getSessionId, sessionId));
        sessionMapper.deleteById(sessionId);
    }

    @Override
    public void feedback(Long messageId, Integer feedback) {
        QaMessage msg = messageMapper.selectById(messageId);
        if (msg == null || !"assistant".equals(msg.getRole())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消息不存在");
        }
        checkSessionAccess(msg.getSessionId());
        QaMessage update = new QaMessage();
        update.setId(messageId);
        update.setFeedback(feedback != null && feedback > 0 ? 1 : -1);
        messageMapper.updateById(update);
    }

    @Override
    public PageResult<QaMessage> pageMessages(PageQuery query, String role, Integer fallback) {
        return PageResult.of(messageMapper.selectPage(query.toPage(), new LambdaQueryWrapper<QaMessage>()
                .like(StringUtils.hasText(query.getKeyword()), QaMessage::getContent, query.getKeyword())
                .eq(StringUtils.hasText(role), QaMessage::getRole, role)
                .eq(fallback != null, QaMessage::getIsFallback, fallback)
                .orderByDesc(QaMessage::getId)));
    }

    @Override
    public Map<String, Object> llmStatus() {
        LlmSettings s = configService.llmSettings();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("configured", s.configured());
        map.put("baseUrl", s.baseUrl());
        map.put("model", s.model());
        map.put("mode", s.configured() ? "LLM + GraphRAG" : "离线兜底（仅GraphRAG检索）");
        return map;
    }

    /** 会话访问校验：会话归属本人、匿名会话，或管理员/馆员 */
    private void checkSessionAccess(Long sessionId) {
        QaSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        Long userId = SecurityUtils.currentUserId();
        if (session.getUserId() != null && !session.getUserId().equals(userId) && !SecurityUtils.isStaff()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该会话");
        }
    }
}
