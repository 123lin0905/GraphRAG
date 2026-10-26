package com.sanxingdui.twin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识图谱可视化数据，结构与 ECharts graph 系列（nodes/links/categories）一致
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识图谱数据（ECharts graph）")
public class GraphVO {

    @Schema(description = "节点")
    private List<Node> nodes = new ArrayList<>();

    @Schema(description = "边")
    private List<Link> links = new ArrayList<>();

    @Schema(description = "节点类别（图例）")
    private List<Category> categories = new ArrayList<>();

    /** 图节点 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "图节点")
    public static class Node {
        @Schema(description = "节点ID（字符串，便于 ECharts 引用）")
        private String id;
        @Schema(description = "节点名称")
        private String name;
        @Schema(description = "类别下标（对应 categories）")
        private Integer category;
        @Schema(description = "实体类型")
        private String entityType;
        @Schema(description = "节点大小")
        private Double symbolSize;
        @Schema(description = "距中心节点的跳数（邻居查询时有效）")
        private Integer hop;
        @Schema(description = "实体描述")
        private String description;
        @Schema(description = "关联业务表")
        private String refTable;
        @Schema(description = "关联业务ID")
        private Long refId;
    }

    /** 图的边 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "图的边")
    public static class Link {
        @Schema(description = "关系ID")
        private String id;
        @Schema(description = "源节点ID")
        private String source;
        @Schema(description = "目标节点ID")
        private String target;
        @Schema(description = "关系名称")
        private String name;
        @Schema(description = "关系类型")
        private String relationType;
        @Schema(description = "权重")
        private Double value;
    }

    /** 节点类别 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "节点类别")
    public static class Category {
        @Schema(description = "类别名称")
        private String name;
        @Schema(description = "实体类型编码")
        private String code;
    }
}
