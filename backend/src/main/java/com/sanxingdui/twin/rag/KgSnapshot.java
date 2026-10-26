package com.sanxingdui.twin.rag;

import com.sanxingdui.twin.entity.KgEntity;
import com.sanxingdui.twin.entity.KgRelation;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识图谱内存快照：实体索引 + 无向邻接表。
 * <p>
 * 三星堆领域图谱规模为百~千级节点，全部加载到内存后做 BFS 子图扩展，
 * 比逐跳查询数据库更快，也便于 GraphRAG 检索时多次复用。
 */
@Getter
public class KgSnapshot {

    /** 实体ID -> 实体 */
    private final Map<Long, KgEntity> entities;
    /** 关系ID -> 关系 */
    private final Map<Long, KgRelation> relations;
    /** 实体ID -> 关联的关系列表（无论出边入边） */
    private final Map<Long, List<KgRelation>> adjacency;
    /** 快照创建时间 */
    private final long createdAt = System.currentTimeMillis();

    public KgSnapshot(List<KgEntity> entityList, List<KgRelation> relationList) {
        Map<Long, KgEntity> em = new LinkedHashMap<>();
        entityList.forEach(e -> em.put(e.getId(), e));
        Map<Long, KgRelation> rm = new LinkedHashMap<>();
        Map<Long, List<KgRelation>> adj = new HashMap<>();
        for (KgRelation r : relationList) {
            // 跳过引用了已删除实体的悬挂关系
            if (!em.containsKey(r.getSourceId()) || !em.containsKey(r.getTargetId())) {
                continue;
            }
            rm.put(r.getId(), r);
            adj.computeIfAbsent(r.getSourceId(), k -> new ArrayList<>()).add(r);
            adj.computeIfAbsent(r.getTargetId(), k -> new ArrayList<>()).add(r);
        }
        this.entities = Collections.unmodifiableMap(em);
        this.relations = Collections.unmodifiableMap(rm);
        this.adjacency = adj;
    }

    /** 获取实体的所有关联关系 */
    public List<KgRelation> relationsOf(Long entityId) {
        return adjacency.getOrDefault(entityId, List.of());
    }

    /** 关系另一端的实体ID */
    public static Long otherEnd(KgRelation r, Long self) {
        return r.getSourceId().equals(self) ? r.getTargetId() : r.getSourceId();
    }
}
