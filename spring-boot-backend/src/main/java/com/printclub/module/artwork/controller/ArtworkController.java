package com.printclub.module.artwork.controller;

import com.printclub.common.annotation.RequireAuth;
import com.printclub.common.annotation.RequireRole;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.Result;
import com.printclub.common.util.SecurityContext;
import com.printclub.module.artwork.dto.ArtworkCreateDTO;
import com.printclub.module.artwork.dto.ArtworkQuery;
import com.printclub.module.artwork.dto.ArtworkUpdateDTO;
import com.printclub.module.artwork.entity.Artwork;
import com.printclub.module.artwork.service.ArtworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 作品库 Controller（M4 - 端用户：所有人能看推荐和详情，社员能看自己作品）
 *
 * @author F
 */
@Tag(name = "作品库")
@RestController
@RequestMapping("/api/artwork")
@RequiredArgsConstructor
public class ArtworkController {

    private final ArtworkService artworkService;

    @Operation(summary = "作品列表（公开）")
    @GetMapping
    @RequireAuth
    public Result<PageResult<Artwork>> list(ArtworkQuery query) {
        return Result.success(artworkService.list(query));
    }

    @Operation(summary = "推荐作品")
    @GetMapping("/recommended")
    @RequireAuth
    public Result<PageResult<Artwork>> recommended(@RequestParam(defaultValue = "12") int limit) {
        return Result.success(artworkService.recommended(limit));
    }

    @Operation(summary = "我的作品")
    @GetMapping("/my")
    @RequireAuth
    public Result<PageResult<Artwork>> my(ArtworkQuery query) {
        query.setAuthorId(SecurityContext.getCurrentUserId());
        return Result.success(artworkService.myArtworks(query));
    }

    @Operation(summary = "作品详情")
    @GetMapping("/{id}")
    @RequireAuth
    public Result<Artwork> detail(@PathVariable("id") Integer id) {
        return Result.success(artworkService.detail(id));
    }

    @Operation(summary = "登记作品（从已完结任务创建）")
    @PostMapping
    @RequireAuth
    public Result<Integer> create(@RequestBody @Valid ArtworkCreateDTO dto) {
        return Result.success(artworkService.create(dto, SecurityContext.getCurrentUserId()));
    }

    @Operation(summary = "更新作品（仅作者）")
    @PutMapping("/{id}")
    @RequireAuth
    public Result<Void> update(@PathVariable("id") Integer id, @RequestBody @Valid ArtworkUpdateDTO dto) {
        artworkService.update(id, dto, SecurityContext.getCurrentUserId());
        return Result.success();
    }

    @Operation(summary = "设置推荐（仅技术骨干+）")
    @PutMapping("/{id}/recommend")
    @RequireAuth
    @RequireRole({1, 2})
    public Result<Void> setRecommend(@PathVariable("id") Integer id, @RequestParam Integer recommended) {
        artworkService.setRecommended(id, recommended);
        return Result.success();
    }

    @Operation(summary = "删除作品（仅作者本人 + 未推荐）")
    @DeleteMapping("/{id}")
    @RequireAuth
    public Result<Void> delete(@PathVariable("id") Integer id) {
        artworkService.delete(id, SecurityContext.getCurrentUserId());
        return Result.success();
    }
}
