package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.common.ResultCode;
import com.sanxingdui.twin.entity.KgEntity;
import com.sanxingdui.twin.entity.KgRelation;
import com.sanxingdui.twin.mapper.KgEntityMapper;
import com.sanxingdui.twin.mapper.KgRelationMapper;
import com.sanxingdui.twin.rag.KgSnapshot;
import com.sanxingdui.twin.service.KnowledgeGraphService;
import com.sanxingdui.twin.vo.GraphVO;
import com.sanxingdui.twin.vo.NameValueVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 知识图谱查询服务实现：基于内存快照的子图检索与可视化数据组装
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {

    /** 快照有效期：图谱在后台被编辑后最多 60 秒自动生效 */
    private static final long SNAPSHOT_TTL_MS = 60_000L;

    /** 实体类型（顺序即 ECharts categories 下标） */
    private static final LinkedHashMap<String, String> TYPE_LABELS = new LinkedHashMap<>();

    static {
        TYPE_LABELS.put("ARTIFACT", "文物");
        TYPE_LABELS.put("PIT", "祭祀坑");
        TYPE_LABELS.put("MATERIAL", "材质");
        TYPE_LABELS.put("PERIOD", "年代");
        TYPE_LABELS.put("CULTURE", "文化");
        TYPE_LABELS.put("THEME", "主题");
        TYPE_LABELS.put("SITE", "遗址");
        TYPE_LABELS.put("MUSEUM", "博物馆");
        TYPE_LABELS.put("HALL", "展厅");
        TYPE_LABELS.put("PERSON", "人物");
        TYPE_LABELS.put("LITERATURE", "文献");
        TYPE_LABELS.put("TECH", "工艺");
    }

    private final KgEntityMapper entityMapper;
    private final KgRelationMapper relationMapper;

    private volatile KgSnapshot cache;

    @Override
    public KgSnapshot snapshot() {
        KgSnapshot s = cache;
        if (s == null || System.currentTimeMillis() - s.getCreatedAt() > SNAPSHOT_TTL_MS) {
            synchronized (this) {
                s = cache;
                if (s == null || System.currentTimeMillis() - s.getCreatedAt() > SNAPSHOT_TTL_MS) {
                    s = new KgSnapshot(entityMapper.selectList(null), relationMapper.selectList(null));
                    cache = s;
                    log.debug("知识图谱快照已刷新：实体 {} 个，关系 {} 条", s.getEntities().size(), s.getRelations().size());
                }
            }
        }
        return s;
    }

    @Override
    public void evict() {
        cache = null;
    }

    @Override
    public GraphVO graph(String types, int limit) {
        KgSnapshot s = snapshot();
        Set<String> typeSet = StringUtils.hasText(types)
                ? Arrays.stream(types.split(",")).map(String::trim).map(String::toUpperCase).collect(Collectors.toSet())
                : Set.of();
        List<Long> ids = s.getEntities().values().stream()
                .filter(e -> typeSet.isEmpty() || typeSet.contains(e.getEntityType()))
                .sorted(Comparator.comparing((KgEntity e) -> e.getWeight() == null ? 0 : e.getWeight().doubleValue()).reversed()
                        .thenComparing(KgEntity::getId))
                .limit(Math.max(1, limit))
                .map(KgEntity::getId).toList();
        return assemble(s, new LinkedHashSet<>(ids), Map.of(), null);
    }

    @Override
    public GraphVO neighbors(Long entityId, int hops, int maxNodes) {
        KgSnapshot s = snapshot();
        if (!s.getEntities().containsKey(entityId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "实体不存在");
        }
        return subgraph(List.of(entityId), hops, maxNodes);
    }

    @Override
    public GraphVO subgraph(Collection<Long> seedIds, int hops, int maxNodes) {
        KgSnapshot s = snapshot();
        int depthLimit = Math.min(Math.max(hops, 1), 3);
        Map<Long, Integer> hopMap = bfs(s, seedIds, depthLimit, Math.max(maxNodes, seedIds.size()));
        return assemble(s, hopMap.keySet(), hopMap, null);
    }

    /**
     * 多源 BFS：逐层扩展，优先保留权重高的邻居，直到达到跳数或节点上限
     *
     * @return 节点ID -> 跳数（按访问顺序）
     */
    private Map<Long, Integer> bfs(KgSnapshot s, Collection<Long> seeds, int maxHop, int maxNodes) {
        Map<Long, Integer> visited = new LinkedHashMap<>();
        Deque<Long> queue = new ArrayDeque<>();
        for (Long seed : seeds) {
            if (s.getEntities().containsKey(seed) && visited.putIfAbsent(seed, 0) == null) {
                queue.add(seed);
            }
        }
        while (!queue.isEmpty() && visited.size() < maxNodes) {
            Long cur = queue.poll();
            int hop = visited.get(cur);
            if (hop >= maxHop) {
                continue;
            }
            List<KgRelation> rels = new ArrayList<>(s.relationsOf(cur));
            rels.sort(Comparator.comparing((KgRelation r) -> r.getWeight() == null ? 0 : r.getWeight().doubleValue()).reversed());
            for (KgRelation r : rels) {
                Long next = KgSnapshot.otherEnd(r, cur);
                if (!visited.containsKey(next)) {
                    visited.put(next, hop + 1);
                    queue.add(next);
                    if (visited.size() >= maxNodes) {
                        break;
                    }
                }
            }
        }
        return visited;
    }

    @Override
    public GraphVO shortestPath(Long fromId, Long toId, int maxDepth) {
        KgSnapshot s = snapshot();
        if (!s.getEntities().containsKey(fromId) || !s.getEntities().containsKey(toId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "实体不存在");
        }
        Map<Long, KgRelation> via = new HashMap<>();
        Map<Long, Integer> depth = new HashMap<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(fromId);
        depth.put(fromId, 0);
        while (!queue.isEmpty() && !depth.containsKey(toId)) {
            Long cur = queue.poll();
            if (depth.get(cur) >= maxDepth) {
                continue;
            }
            for (KgRelation r : s.relationsOf(cur)) {
                Long next = KgSnapshot.otherEnd(r, cur);
                if (!depth.containsKey(next)) {
                    depth.put(next, depth.get(cur) + 1);
                    via.put(next, r);
                    queue.add(next);
                }
            }
        }
        if (!depth.containsKey(toId)) {
            return new GraphVO(new ArrayList<>(), new ArrayList<>(), buildCategories());
        }
        // 回溯路径
        LinkedHashSet<Long> nodes = new LinkedHashSet<>();
        Set<Long> relIds = new HashSet<>();
        Long cur = toId;
        nodes.add(cur);
        while (!cur.equals(fromId)) {
            KgRelation r = via.get(cur);
            relIds.add(r.getId());
            cur = KgSnapshot.otherEnd(r, cur);
            nodes.add(cur);
        }
        return assemble(s, nodes, depth, relIds);
    }

    @Override
    public List<KgEntity> search(String keyword, int limit) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        return entityMapper.selectList(new LambdaQueryWrapper<KgEntity>()
                .like(KgEntity::getName, keyword).or().like(KgEntity::getAliases, keyword)
                .orderByDesc(KgEntity::getWeight).last("LIMIT " + Math.min(Math.max(limit, 1), 100)));
    }

    @Override
    public List<NameValueVO> typeStatistics() {
        Map<String, Long> counts = snapshot().getEntities().values().stream()
                .collect(Collectors.groupingBy(KgEntity::getEntityType, Collectors.counting()));
        return TYPE_LABELS.entrySet().stream().filter(e -> counts.containsKey(e.getKey()))
                .map(e -> new NameValueVO(e.getValue(), counts.get(e.getKey()))).toList();
    }

    @Override
    public List<NameValueVO> relationStatistics() {
        return snapshot().getRelations().values().stream()
                .collect(Collectors.groupingBy(KgRelation::getRelationName, LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream().map(e -> new NameValueVO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(NameValueVO::getValue).reversed()).toList();
    }

    @Override
    public String typeLabel(String type) {
        return TYPE_LABELS.getOrDefault(type, type);
    }

    /**
     * 组装 ECharts 图数据：给定节点集合，收集集合内部的边
     *
     * @param onlyRelIds 非空时只保留这些边（最短路径场景）
     */
    private GraphVO assemble(KgSnapshot s, Set<Long> nodeIds, Map<Long, Integer> hopMap, Set<Long> onlyRelIds) {
        List<String> typeIndex = new ArrayList<>(TYPE_LABELS.keySet());
        GraphVO vo = new GraphVO();
        vo.setCategories(buildCategories());
        for (Long id : nodeIds) {
            KgEntity e = s.getEntities().get(id);
            double weight = e.getWeight() == null ? 1.0 : e.getWeight().doubleValue();
            int idx = typeIndex.indexOf(e.getEntityType());
            vo.getNodes().add(new GraphVO.Node(String.valueOf(id), e.getName(), idx < 0 ? 0 : idx, e.getEntityType(),
                    12 + weight * 8, hopMap.get(id), e.getDescription(), e.getRefTable(), e.getRefId()));
        }
        Set<Long> added = new HashSet<>();
        for (Long id : nodeIds) {
            for (KgRelation r : s.relationsOf(id)) {
                if (added.contains(r.getId()) || !nodeIds.contains(r.getSourceId()) || !nodeIds.contains(r.getTargetId())) {
                    continue;
                }
                if (onlyRelIds != null && !onlyRelIds.contains(r.getId())) {
                    continue;
                }
                added.add(r.getId());
                vo.getLinks().add(new GraphVO.Link(String.valueOf(r.getId()), String.valueOf(r.getSourceId()),
                        String.valueOf(r.getTargetId()), r.getRelationName(), r.getRelationType(),
                        r.getWeight() == null ? 1.0 : r.getWeight().doubleValue()));
            }
        }
        return vo;
    }

    private List<GraphVO.Category> buildCategories() {
        return TYPE_LABELS.entrySet().stream().map(e -> new GraphVO.Category(e.getValue(), e.getKey()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
