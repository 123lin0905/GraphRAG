package com.sanxingdui.twin.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 通用分页查询参数
 */
@Data
@Schema(description = "通用分页查询参数")
public class PageQuery {

    @Schema(description = "页码，从1开始", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private long current = 1;

    @Schema(description = "每页条数", example = "10")
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 200, message = "每页条数最大为200")
    private long size = 10;

    @Schema(description = "搜索关键词（模糊匹配）")
    private String keyword;

    /** 转换为 MyBatis-Plus 分页对象 */
    public <T> Page<T> toPage() {
        return new Page<>(current, size);
    }
}
