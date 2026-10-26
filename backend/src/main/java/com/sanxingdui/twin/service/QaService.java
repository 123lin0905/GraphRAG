package com.sanxingdui.twin.service;

import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.dto.QaAskDTO;
import com.sanxingdui.twin.entity.QaMessage;
import com.sanxingdui.twin.entity.QaSession;
import com.sanxingdui.twin.rag.RetrievalResult;
import com.sanxingdui.twin.vo.QaAnswerVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * GraphRAG 智能问答服务
 */
public interface QaService {

    /** 同步问答 */
    QaAnswerVO ask(QaAskDTO dto, String clientIp);

    /** 流式问答（SSE） */
    SseEmitter askStream(QaAskDTO dto, String clientIp);

    /** 仅执行检索（调试/可视化用） */
    RetrievalResult retrieveOnly(String question);

    /** 当前用户的会话列表 */
    List<QaSession> mySessions();

    /** 会话消息列表 */
    List<QaMessage> messages(Long sessionId);

    /** 删除会话 */
    void deleteSession(Long sessionId);

    /** 消息反馈 */
    void feedback(Long messageId, Integer feedback);

    /** 管理端：分页查询全部问答消息 */
    PageResult<QaMessage> pageMessages(PageQuery query, String role, Integer fallback);

    /** 大模型配置状态 */
    Map<String, Object> llmStatus();
}
