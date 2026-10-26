package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.annotation.OperationLog;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.dto.ArtifactQuery;
import com.sanxingdui.twin.entity.Artifact;
import com.sanxingdui.twin.service.ArtifactService;
import com.sanxingdui.twin.vo.ArtifactDetailVO;
import com.sanxingdui.twin.vo.ArtifactVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文物接口：继承通用增删改，覆盖分页查询为多表关联查询
 */
@Tag(name = "文物管理", description = "文物多条件检索、详情（含图片/3DGS模型/图谱邻居）、增删改")
@RestController
@RequestMapping("/api/artifacts")
@RequiredArgsConstructor
public class ArtifactController extends BaseCrudController<Artifact, ArtifactService> {

    private final ArtifactService artifactService;

    @Operation(summary = "文物多条件分页查询",
            description = "支持关键词（名称/别名/编号/描述）、分类（含子分类）、祭祀坑、材质、年代、展厅、镇馆之宝、状态过滤；orderBy=hot 按热度")
    @GetMapping("/search")
    public Result<PageResult<ArtifactVO>> search(@Valid ArtifactQuery query) {
        return Result.success(artifactService.pageVo(query));
    }

    @Operation(summary = "文物详情", description = "包含图片、3DGS模型、知识图谱一跳邻居、文献、修复记录、相关文物、评分、收藏状态，并累计浏览量")
    @GetMapping("/{id}/detail")
    public Result<ArtifactDetailVO> detail(@PathVariable Long id,
                                           @RequestParam(defaultValue = "true") boolean countView) {
        return Result.success(artifactService.detail(id, countView));
    }

    @Operation(summary = "热门文物排行")
    @GetMapping("/hot")
    public Result<List<ArtifactVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(artifactService.hot(limit));
    }

    @Override
    protected List<String> keywordColumns() {
        return List.of("name", "alias", "artifact_no");
    }

    @Override
    @OperationLog(module = "文物管理", value = "新增文物")
    public Result<Artifact> create(Artifact entity) {
        return super.create(entity);
    }

    @Override
    @OperationLog(module = "文物管理", value = "修改文物")
    public Result<Artifact> update(Long id, Artifact entity) {
        return super.update(id, entity);
    }

    @Override
    @OperationLog(module = "文物管理", value = "删除文物")
    public Result<Void> delete(Long id) {
        return super.delete(id);
    }
}
