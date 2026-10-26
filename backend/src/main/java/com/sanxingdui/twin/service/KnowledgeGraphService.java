package com.sanxingdui.twin.service;

import com.sanxingdui.twin.entity.KgEntity;
import com.sanxingdui.twin.rag.KgSnapshot;
import com.sanxingdui.twin.vo.GraphVO;
import com.sanxingdui.twin.vo.NameValueVO;

import java.util.Collection;
import java.util.List;

/**
 * 知识图谱查询服务
 */
public interface KnowledgeGraphService {

    /** 获取图谱内存快照（带过期自动刷新） */
    KgSnapshot snapshot();

    /** 主动清除快照缓存（图谱数据变更后调用） */
    void evict();

    /**
     * 全图（或按实体类型过滤）
     *
     * @param types 实体类型，逗号分隔，空表示全部
     * @param limit 最大节点数（按重要度截取）
     */
    GraphVO graph(String types, int limit);

    /** 以某实体为中心的 n 跳邻居子图 */
    GraphVO neighbors(Long entityId, int hops, int maxNodes);

    /** 多个种子实体的 n 跳子图（GraphRAG 使用） */
    GraphVO subgraph(Collection<Long> seedIds, int hops, int maxNodes);

    /** 两实体间最短路径（无向 BFS） */
    GraphVO shortestPath(Long fromId, Long toId, int maxDepth);

    /** 按名称/别名搜索实体 */
    List<KgEntity> search(String keyword, int limit);

    /** 各实体类型数量 */
    List<NameValueVO> typeStatistics();

    /** 各关系类型数量 */
    List<NameValueVO> relationStatistics();

    /** 实体类型编码 -> 中文名 */
    String typeLabel(String type);
}
