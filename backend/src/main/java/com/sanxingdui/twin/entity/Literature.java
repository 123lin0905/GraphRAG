package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文献资料表（literature）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("literature")
@Schema(description = "文献资料表")
public class Literature extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 文献标题 */
    @Schema(description = "文献标题")
    private String title;

    /** 作者/编者 */
    @Schema(description = "作者/编者")
    private String authors;

    /** 出处（出版社/期刊/古籍） */
    @Schema(description = "出处（出版社/期刊/古籍）")
    private String source;

    /** 出版年份或朝代 */
    @Schema(description = "出版年份或朝代")
    private String publishYear;

    /** 文献类型：BOOK-专著 PAPER-论文 ANCIENT-古籍 REPORT-考古报告 GUIDE-导览文稿 */
    @Schema(description = "文献类型：BOOK-专著 PAPER-论文 ANCIENT-古籍 REPORT-考古报告 GUIDE-导览文稿")
    private String litType;

    /** 关键词（逗号分隔） */
    @Schema(description = "关键词（逗号分隔）")
    private String keywords;

    /** 摘要 */
    @Schema(description = "摘要")
    private String abstractText;

    /** 正文/摘录（GraphRAG文本检索片段来源） */
    @Schema(description = "正文/摘录（GraphRAG文本检索片段来源）")
    private String content;

    /** 原文链接 */
    @Schema(description = "原文链接")
    private String url;
}
