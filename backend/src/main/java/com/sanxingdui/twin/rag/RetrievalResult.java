package com.sanxingdui.twin.rag;

import com.sanxingdui.twin.vo.GraphVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GraphRAG 检索结果：链接到的实体、子图、三元组、文物资料、文献片段
 */
@Data
public class RetrievalResult {

    /** 实体链接方式：EXACT-名称/别名命中 FUZZY-字符相似度兜底 NONE-未命中 */
    private String linkMode = "NONE";

    /** 问句中识别出的种子实体（名称） */
    private List<String> seedEntities = new ArrayList<>();

    /** 种子实体ID */
    private List<Long> seedIds = new ArrayList<>();

    /** 种子实体描述：名称 -> “类型：描述” */
    private Map<String, String> entityDescriptions = new LinkedHashMap<>();

    /** 检索子图 */
    private GraphVO subgraph = new GraphVO();

    /** 三元组文本（头 -关系-> 尾） */
    private List<String> triples = new ArrayList<>();

    /** 文物资料片段 */
    private List<String> artifactSnippets = new ArrayList<>();

    /** 文献片段 */
    private List<LiteratureSnippet> literatureSnippets = new ArrayList<>();

    /** 文献片段 */
    public record LiteratureSnippet(Long id, String title, String source, String snippet) {
    }

    /** 是否检索到任何知识 */
    public boolean isEmpty() {
        return seedIds.isEmpty() && artifactSnippets.isEmpty() && literatureSnippets.isEmpty();
    }

    /** 拼装注入提示词的上下文文本 */
    public String toPromptContext() {
        StringBuilder sb = new StringBuilder();
        if (!entityDescriptions.isEmpty()) {
            sb.append("【相关实体】\n");
            entityDescriptions.forEach((k, v) -> sb.append("- ").append(k).append("（").append(v).append("）\n"));
        }
        if (!triples.isEmpty()) {
            sb.append("\n【知识图谱三元组】\n");
            triples.forEach(t -> sb.append("- ").append(t).append('\n'));
        }
        if (!artifactSnippets.isEmpty()) {
            sb.append("\n【文物资料】\n");
            artifactSnippets.forEach(a -> sb.append("- ").append(a).append('\n'));
        }
        if (!literatureSnippets.isEmpty()) {
            sb.append("\n【文献摘录】\n");
            literatureSnippets.forEach(l -> sb.append("- 《").append(l.title()).append("》")
                    .append(l.source() == null ? "" : "（" + l.source() + "）").append("：").append(l.snippet()).append('\n'));
        }
        return sb.toString();
    }
}
