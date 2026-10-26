package com.sanxingdui.twin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sanxingdui.twin.common.BaseEntity;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.common.ResultCode;
import com.sanxingdui.twin.security.LoginUser;
import com.sanxingdui.twin.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 通用 CRUD 控制器基类。
 * <p>
 * 子类只需声明可模糊搜索的列 {@link #keywordColumns()}、可等值过滤的参数 {@link #filterColumns()}，
 * 即可获得 分页 / 列表 / 详情 / 新增 / 修改 / 删除 / 批量删除 接口。
 * 列名均来自子类白名单，不会把前端传入的字段名直接拼进 SQL，避免注入风险。
 *
 * @param <T> 实体类型
 * @param <S> 服务类型
 */
public abstract class BaseCrudController<T extends BaseEntity, S extends IService<T>> {

    @Autowired
    protected S service;

    /** 关键词模糊匹配的数据库列 */
    protected List<String> keywordColumns() {
        return List.of();
    }

    /** 允许等值过滤的参数：请求参数名(驼峰) -> 数据库列名 */
    protected Map<String, String> filterColumns() {
        return Map.of();
    }

    /** 默认排序列（降序）；返回 null 表示按 id 升序 */
    protected String defaultOrderColumn() {
        return null;
    }

    /** 有权进行写操作的角色，默认管理员与馆员 */
    protected String[] writeRoles() {
        return new String[]{"ADMIN", "CURATOR"};
    }

    /** 有权进行读操作的角色，空数组表示公开 */
    protected String[] readRoles() {
        return new String[0];
    }

    /** 构建查询条件，子类可覆盖以追加自定义条件 */
    protected QueryWrapper<T> buildQuery(PageQuery query, Map<String, String> params) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        List<String> kwCols = keywordColumns();
        if (StringUtils.hasText(query.getKeyword()) && !kwCols.isEmpty()) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> {
                for (int i = 0; i < kwCols.size(); i++) {
                    if (i > 0) {
                        w.or();
                    }
                    w.like(kwCols.get(i), kw);
                }
            });
        }
        filterColumns().forEach((param, column) -> {
            String value = params.get(param);
            if (StringUtils.hasText(value)) {
                wrapper.eq(column, value);
            }
        });
        String order = defaultOrderColumn();
        if (order != null) {
            wrapper.orderByDesc(order);
        } else {
            wrapper.orderByAsc("id");
        }
        return wrapper;
    }

    @Operation(summary = "分页查询", description = "支持 keyword 关键词搜索及子类声明的等值过滤参数")
    @GetMapping("/page")
    public Result<PageResult<T>> page(@Valid PageQuery query,
                                      @Parameter(hidden = true) @RequestParam Map<String, String> params) {
        checkRoles(readRoles());
        return Result.success(PageResult.of(service.page(query.toPage(), buildQuery(query, params))));
    }

    @Operation(summary = "列表查询（不分页）")
    @GetMapping("/list")
    public Result<List<T>> list(PageQuery query, @Parameter(hidden = true) @RequestParam Map<String, String> params) {
        checkRoles(readRoles());
        return Result.success(service.list(buildQuery(query, params)));
    }

    @Operation(summary = "根据ID查询详情")
    @GetMapping("/{id}")
    public Result<T> getById(@PathVariable Long id) {
        checkRoles(readRoles());
        T entity = service.getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return Result.success(entity);
    }

    @Operation(summary = "新增")
    @PostMapping
    public Result<T> create(@Valid @RequestBody T entity) {
        checkRoles(writeRoles());
        entity.setId(null);
        beforeSave(entity, true);
        service.save(entity);
        return Result.success("新增成功", entity);
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public Result<T> update(@PathVariable Long id, @Valid @RequestBody T entity) {
        checkRoles(writeRoles());
        if (service.getById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        entity.setId(id);
        beforeSave(entity, false);
        service.updateById(entity);
        return Result.success("修改成功", service.getById(id));
    }

    @Operation(summary = "删除（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkRoles(writeRoles());
        if (!service.removeById(id)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return Result.success();
    }

    @Operation(summary = "批量删除（逻辑删除）", description = "ids 为逗号分隔的主键")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestParam String ids) {
        checkRoles(writeRoles());
        List<Long> idList = Arrays.stream(ids.split(",")).map(String::trim).filter(StringUtils::hasText)
                .map(Long::valueOf).toList();
        service.removeByIds(idList);
        return Result.success();
    }

    /** 保存前钩子，子类可做字段校验或默认值处理 */
    protected void beforeSave(T entity, boolean isNew) {
    }

    /** 校验当前用户是否具备任一角色 */
    protected void checkRoles(String[] roles) {
        if (roles == null || roles.length == 0) {
            return;
        }
        LoginUser user = SecurityUtils.currentUser();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        boolean ok = Arrays.stream(roles).anyMatch(user.getRoles()::contains);
        if (!ok) {
            throw new AccessDeniedException("权限不足");
        }
    }
}
