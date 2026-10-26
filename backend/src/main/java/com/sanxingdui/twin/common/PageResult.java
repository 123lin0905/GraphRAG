package com.sanxingdui.twin.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * 分页结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码")
    private long current;

    @Schema(description = "每页条数")
    private long size;

    @Schema(description = "数据列表")
    private List<T> records;

    /** 由 MyBatis-Plus 分页对象构建 */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /** 由 MyBatis-Plus 分页对象构建，并转换记录类型 */
    public static <T, R> PageResult<R> of(IPage<T> page, Function<T, R> converter) {
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(converter).toList());
    }
}
