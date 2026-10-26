package com.sanxingdui.twin.rag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sanxingdui.twin.config.RagProperties;
import com.sanxingdui.twin.entity.ArtifactLiterature;
import com.sanxingdui.twin.entity.KgEntity;
import com.sanxingdui.twin.entity.KgRelation;
import com.sanxingdui.twin.entity.Literature;
import com.sanxingdui.twin.mapper.ArtifactLiteratureMapper;
import com.sanxingdui.twin.mapper.ArtifactMapper;
import com.sanxingdui.twin.mapper.LiteratureMapper;
import com.sanxingdui.twin.service.KnowledgeGraphService;
import com.sanxingdui.twin.service.SystemConfigService;
import com.sanxingdui.twin.vo.ArtifactVO;
import com.sanxingdui.twin.vo.GraphVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GraphRAG 检索器。检索流程：
 * <ol>
 *     <li>实体链接：用知识图谱实体名称与别名在问句中做最长匹配；未命中时用字符二元组相似度兜底；</li>
 *     <li>子图扩展：以命中实体为种子做 1~2 跳 BFS，得到局部子图；</li>
 *     <li>三元组排序：与种子直接相连的边优先，其次按关系权重；</li>
 *     <li>文本补充：取相关文物的描述与价值说明、相关文献的命中片段；</li>
 *     <li>组装上下文：交给大模型生成回答，或在无 API Key 时生成离线兜底回答。</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraphRagRetriever {

    /** 实体链接时忽略的过短或泛化词 */
    private static final Set<String> STOP_TERMS = Set.of("三星", "文物", "什么", "哪些", "哪个", "介绍");

    /** 模糊匹配时忽略的常见疑问二元组 */
    private static final Set<String> STOP_BIGRAMS = Set.of("什么", "哪些", "哪个", "一下", "介绍", "请问", "怎么", "为什",
            "么样", "有没", "没有", "可以", "告诉", "我想", "知道", "是谁", "有什", "关于", "的是", "了解");

    /** 片段截取窗口（字符） */
    private static final int SNIPPET_WINDOW = 120;

    private final KnowledgeGraphService kgService;
    private final ArtifactMapper artifactMapper;
    private final LiteratureMapper literatureMapper;
    private final ArtifactLiteratureMapper artifactLiteratureMapper;
    private final SystemConfigService configService;
    private final RagProperties ragProperties;

    /**
     * 执行检索
     *
     * @param question 用户问题
     */
    public RetrievalResult retrieve(String question) {
        RetrievalResult result = new RetrievalResult();
        String q = question == null ? "" : question.trim();
        if (q.isEmpty()) {
            return result;
        }
        KgSnapshot snapshot = kgService.snapshot();
        int maxHops = configService.getInt("rag.max_hops", ragProperties.getMaxHops());
        int maxTriples = configService.getInt("rag.max_triples", ragProperties.getMaxTriples());
        int maxLiterature = configService.getInt("rag.max_literature", ragProperties.getMaxLiterature());

        // 1. 实体链接
        List<KgEntity> seeds = new ArrayList<>(linkEntities(q, snapshot));
        result.setLinkMode(seeds.isEmpty() ? "NONE" : "EXACT");
        if (seeds.isEmpty()) {
            seeds = new ArrayList<>(fuzzyLink(q, snapshot, 3));
            if (!seeds.isEmpty()) {
                result.setLinkMode("FUZZY");
            }
        } else if (seeds.size() == 1) {
            // 精确命中过少时，用高置信度模糊匹配补充（如“崇拜太阳”补充到“太阳崇拜”主题）
            Set<Long> exists = new HashSet<>();
            seeds.forEach(e -> exists.add(e.getId()));
            for (KgEntity e : fuzzyLink(q, snapshot, 6)) {
                if (exists.add(e.getId())) {
                    seeds.add(e);
                    result.setLinkMode("EXACT+FUZZY");
                }
            }
        }
        if (seeds.isEmpty()) {
            return result;
        }
        for (KgEntity e : seeds) {
            result.getSeedIds().add(e.getId());
            result.getSeedEntities().add(e.getName());
            String desc = e.getDescription() == null ? "" : "：" + truncate(e.getDescription(), 160);
            result.getEntityDescriptions().put(e.getName(), kgService.typeLabel(e.getEntityType()) + desc);
        }

        // 2. 子图扩展：种子较多时只扩展 1 跳，避免上下文过长
        int hops = seeds.size() > 3 ? 1 : Math.min(Math.max(maxHops, 1), 2);
        int maxNodes = Math.max(20, maxTriples);
        GraphVO subgraph = kgService.subgraph(result.getSeedIds(), hops, maxNodes);
        result.setSubgraph(subgraph);

        // 3. 三元组排序与文本化
        Set<String> seedIdStr = new HashSet<>();
        result.getSeedIds().forEach(id -> seedIdStr.add(String.valueOf(id)));
        Map<String, String> nameById = new HashMap<>();
        subgraph.getNodes().forEach(n -> nameById.put(n.getId(), n.getName()));
        subgraph.getLinks().stream()
                .sorted(Comparator.comparing((GraphVO.Link l) ->
                                seedIdStr.contains(l.getSource()) || seedIdStr.contains(l.getTarget()) ? 0 : 1)
                        .thenComparing(GraphVO.Link::getValue, Comparator.reverseOrder()))
                .limit(maxTriples)
                .forEach(l -> result.getTriples().add(
                        nameById.get(l.getSource()) + " -" + l.getName() + "-> " + nameById.get(l.getTarget())));

        // 4. 文物资料：种子中的文物 + 非文物种子一跳内的文物
        LinkedHashSet<Long> artifactIds = new LinkedHashSet<>();
        for (KgEntity e : seeds) {
            if ("artifact".equals(e.getRefTable()) && e.getRefId() != null) {
                artifactIds.add(e.getRefId());
            }
        }
        for (KgEntity e : seeds) {
            if (!"ARTIFACT".equals(e.getEntityType())) {
                for (KgRelation r : snapshot.relationsOf(e.getId())) {
                    KgEntity other = snapshot.getEntities().get(KgSnapshot.otherEnd(r, e.getId()));
                    if (other != null && "artifact".equals(other.getRefTable()) && other.getRefId() != null) {
                        artifactIds.add(other.getRefId());
                    }
                }
            }
        }
        List<Long> topArtifacts = artifactIds.stream().limit(ragProperties.getMaxArtifacts()).toList();
        if (!topArtifacts.isEmpty()) {
            List<ArtifactVO> artifacts = artifactMapper.selectVoByIds(topArtifacts);
            // 保持“种子优先”的顺序
            artifacts.sort(Comparator.comparingInt(a -> topArtifacts.indexOf(a.getId())));
            artifacts.forEach(a -> result.getArtifactSnippets().add(formatArtifact(a)));
        }

        // 5. 文献片段
        result.setLiteratureSnippets(searchLiterature(seeds, topArtifacts, maxLiterature));
        log.debug("GraphRAG 检索完成：模式={}，种子={}，三元组={}，文物={}，文献={}", result.getLinkMode(),
                result.getSeedEntities(), result.getTriples().size(), result.getArtifactSnippets().size(),
                result.getLiteratureSnippets().size());
        return result;
    }

    /**
     * 基于名称/别名的实体链接（最长匹配优先，被更长命中词包含的短词丢弃）
     */
    private List<KgEntity> linkEntities(String question, KgSnapshot snapshot) {
        // 命中词 -> 实体（同一命中词可能对应多个实体，取权重高者）
        Map<String, KgEntity> hits = new LinkedHashMap<>();
        for (KgEntity e : snapshot.getEntities().values()) {
            for (String term : termsOf(e)) {
                if (term.length() >= 2 && !STOP_TERMS.contains(term) && question.contains(term)) {
                    KgEntity old = hits.get(term);
                    if (old == null || weight(e) > weight(old)) {
                        hits.put(term, e);
                    }
                }
            }
        }
        List<String> terms = new ArrayList<>(hits.keySet());
        terms.sort(Comparator.comparingInt(String::length).reversed());
        List<String> kept = new ArrayList<>();
        for (String t : terms) {
            boolean covered = kept.stream().anyMatch(k -> k.contains(t) && !hits.get(k).getId().equals(hits.get(t).getId()));
            if (!covered) {
                kept.add(t);
            }
        }
        LinkedHashMap<Long, KgEntity> result = new LinkedHashMap<>();
        // 按在问句中出现的位置排序，更符合用户意图
        kept.sort(Comparator.comparingInt(question::indexOf));
        kept.forEach(t -> result.putIfAbsent(hits.get(t).getId(), hits.get(t)));
        return result.values().stream().limit(6).toList();
    }

    /**
     * 兜底模糊链接：统计问句字符二元组在实体名称/别名/描述中的命中情况
     * （名称命中一个二元组记3分，描述命中记1分）
     *
     * @param minScore 最低得分阈值
     */
    private List<KgEntity> fuzzyLink(String question, KgSnapshot snapshot, double minScore) {
        Set<String> grams = new LinkedHashSet<>();
        String cleaned = question.replaceAll("[\\p{Punct}\\s，。？！、；：“”‘’（）《》]", "");
        for (int i = 0; i + 2 <= cleaned.length(); i++) {
            String g = cleaned.substring(i, i + 2);
            if (!STOP_BIGRAMS.contains(g)) {
                grams.add(g);
            }
        }
        if (grams.isEmpty()) {
            return List.of();
        }
        record Scored(KgEntity entity, double score) {
        }
        List<Scored> scored = new ArrayList<>();
        for (KgEntity e : snapshot.getEntities().values()) {
            String names = String.join("|", termsOf(e));
            String desc = e.getDescription() == null ? "" : e.getDescription();
            double s = 0;
            for (String g : grams) {
                if (names.contains(g)) {
                    s += 3;
                } else if (desc.contains(g)) {
                    s += 1;
                }
            }
            if (s >= minScore) {
                scored.add(new Scored(e, s + weight(e) * 0.1));
            }
        }
        return scored.stream().sorted(Comparator.comparingDouble(Scored::score).reversed())
                .limit(3).map(Scored::entity).toList();
    }

    /** 检索文献片段：文物关联文献优先，其次全文命中实体名称的文献 */
    private List<RetrievalResult.LiteratureSnippet> searchLiterature(List<KgEntity> seeds, List<Long> artifactIds, int limit) {
        LinkedHashMap<Long, Literature> candidates = new LinkedHashMap<>();
        Set<Long> linkedIds = new HashSet<>();
        if (!artifactIds.isEmpty()) {
            List<Long> litIds = artifactLiteratureMapper.selectList(new LambdaQueryWrapper<ArtifactLiterature>()
                            .in(ArtifactLiterature::getArtifactId, artifactIds))
                    .stream().map(ArtifactLiterature::getLiteratureId).distinct().toList();
            linkedIds.addAll(litIds);
            if (!litIds.isEmpty()) {
                literatureMapper.selectBatchIds(litIds).forEach(l -> candidates.putIfAbsent(l.getId(), l));
            }
        }
        for (KgEntity e : seeds) {
            if (candidates.size() >= limit * 3) {
                break;
            }
            String name = e.getName().replace("《", "").replace("》", "");
            literatureMapper.selectList(new LambdaQueryWrapper<Literature>()
                            .like(Literature::getContent, name).or().like(Literature::getKeywords, name)
                            .or().like(Literature::getTitle, name).last("LIMIT 3"))
                    .forEach(l -> candidates.putIfAbsent(l.getId(), l));
        }
        List<String> terms = seeds.stream().flatMap(e -> termsOf(e).stream()).filter(t -> t.length() >= 2).toList();
        // 打分排序：与文物直接关联的文献 +2，片段命中种子词 +1
        return candidates.values().stream()
                .map(l -> new RetrievalResult.LiteratureSnippet(l.getId(), l.getTitle(),
                        joinSource(l), snippet(l.getContent(), terms)))
                .sorted(Comparator.comparingInt((RetrievalResult.LiteratureSnippet s) ->
                        (linkedIds.contains(s.id()) ? 2 : 0) + (terms.stream().anyMatch(s.snippet()::contains) ? 1 : 0)).reversed())
                .limit(limit).toList();
    }

    private String formatArtifact(ArtifactVO a) {
        StringBuilder sb = new StringBuilder(a.getName());
        List<String> attrs = new ArrayList<>();
        if (a.getPitName() != null) {
            attrs.add("出土：" + a.getPitName());
        }
        if (a.getMaterialName() != null) {
            attrs.add("材质：" + a.getMaterialName());
        }
        if (a.getPeriodName() != null) {
            attrs.add("年代：" + a.getPeriodName());
        }
        if (StringUtils.hasText(a.getDimensions())) {
            attrs.add("尺寸：" + a.getDimensions());
        }
        if (a.getHallName() != null) {
            attrs.add("陈列：" + a.getHallName());
        }
        if (!attrs.isEmpty()) {
            sb.append("（").append(String.join("；", attrs)).append("）");
        }
        if (StringUtils.hasText(a.getDescription())) {
            sb.append("。").append(truncate(a.getDescription(), 220));
        }
        if (StringUtils.hasText(a.getCulturalValue())) {
            sb.append(" 价值：").append(truncate(a.getCulturalValue(), 160));
        }
        return sb.toString();
    }

    /** 以第一个命中词为中心截取片段 */
    private String snippet(String content, List<String> terms) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        int pos = -1;
        for (String t : terms) {
            int p = content.indexOf(t);
            if (p >= 0 && (pos < 0 || p < pos)) {
                pos = p;
            }
        }
        if (pos < 0) {
            return truncate(content, SNIPPET_WINDOW * 2);
        }
        int start = Math.max(0, pos - SNIPPET_WINDOW / 2);
        int end = Math.min(content.length(), pos + SNIPPET_WINDOW * 3 / 2);
        return (start > 0 ? "…" : "") + content.substring(start, end) + (end < content.length() ? "…" : "");
    }

    private List<String> termsOf(KgEntity e) {
        List<String> terms = new ArrayList<>();
        terms.add(e.getName().replace("《", "").replace("》", ""));
        if (StringUtils.hasText(e.getAliases())) {
            for (String a : e.getAliases().split("[,，、;；]")) {
                if (StringUtils.hasText(a)) {
                    terms.add(a.trim());
                }
            }
        }
        return terms;
    }

    private String joinSource(Literature l) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(l.getAuthors())) {
            parts.add(l.getAuthors());
        }
        if (StringUtils.hasText(l.getPublishYear())) {
            parts.add(l.getPublishYear());
        }
        return parts.isEmpty() ? null : String.join("，", parts);
    }

    private double weight(KgEntity e) {
        return e.getWeight() == null ? 1.0 : e.getWeight().doubleValue();
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
