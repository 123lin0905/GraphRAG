package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.common.PageQuery;
import com.sanxingdui.twin.common.PageResult;
import com.sanxingdui.twin.common.Result;
import com.sanxingdui.twin.dto.FavoriteDTO;
import com.sanxingdui.twin.entity.Favorite;
import com.sanxingdui.twin.service.FavoriteService;
import com.sanxingdui.twin.vo.ArtifactVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 收藏接口（需登录）
 */
@Tag(name = "我的收藏")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "收藏/取消收藏（切换）", description = "返回 favorited 表示切换后的状态")
    @PostMapping("/toggle")
    public Result<Map<String, Boolean>> toggle(@Valid @RequestBody FavoriteDTO dto) {
        return Result.success(Map.of("favorited", favoriteService.toggle(dto)));
    }

    @Operation(summary = "是否已收藏")
    @GetMapping("/check")
    public Result<Map<String, Boolean>> check(@RequestParam(defaultValue = "ARTIFACT") String targetType,
                                              @RequestParam Long targetId) {
        return Result.success(Map.of("favorited", favoriteService.isFavorited(targetType, targetId)));
    }

    @Operation(summary = "我收藏的文物（分页）")
    @GetMapping("/artifacts")
    public Result<PageResult<ArtifactVO>> myArtifacts(@Valid PageQuery query) {
        return Result.success(favoriteService.myArtifacts(query));
    }

    @Operation(summary = "我的全部收藏记录")
    @GetMapping
    public Result<List<Favorite>> list(@RequestParam(required = false) String targetType) {
        return Result.success(favoriteService.myFavorites(targetType));
    }
}
