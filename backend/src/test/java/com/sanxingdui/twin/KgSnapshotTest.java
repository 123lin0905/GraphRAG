package com.sanxingdui.twin;

import com.sanxingdui.twin.entity.KgEntity;
import com.sanxingdui.twin.entity.KgRelation;
import com.sanxingdui.twin.rag.KgSnapshot;
import com.sanxingdui.twin.rag.LlmSettings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 不依赖数据库的单元测试：图谱快照邻接表与大模型配置
 */
class KgSnapshotTest {

    private KgEntity entity(long id, String name) {
        KgEntity e = new KgEntity();
        e.setId(id);
        e.setName(name);
        e.setEntityType("ARTIFACT");
        return e;
    }

    private KgRelation relation(long id, long s, long t) {
        KgRelation r = new KgRelation();
        r.setId(id);
        r.setSourceId(s);
        r.setTargetId(t);
        r.setRelationName("出土于");
        return r;
    }

    @Test
    void adjacencyIsUndirectedAndSkipsDanglingRelations() {
        KgSnapshot s = new KgSnapshot(List.of(entity(1, "青铜神树"), entity(2, "二号祭祀坑")),
                List.of(relation(10, 1, 2), relation(11, 1, 99)));
        assertEquals(1, s.getRelations().size(), "引用不存在实体的关系应被忽略");
        assertEquals(1, s.relationsOf(1L).size());
        assertEquals(1, s.relationsOf(2L).size());
        assertEquals(2L, KgSnapshot.otherEnd(s.relationsOf(1L).get(0), 1L));
    }

    @Test
    void llmSettingsBuildsChatUrl() {
        LlmSettings deepseek = new LlmSettings("https://api.deepseek.com/", "sk-x", "deepseek-chat", 0.3, 512);
        assertEquals("https://api.deepseek.com/chat/completions", deepseek.chatUrl());
        assertTrue(deepseek.configured());
        LlmSettings empty = new LlmSettings("https://api.openai.com/v1", "", "gpt-4o-mini", 0.3, 512);
        assertEquals("https://api.openai.com/v1/chat/completions", empty.chatUrl());
        assertFalse(empty.configured());
    }
}
