package com.printclub.module.artwork.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printclub.common.exception.BusinessException;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.ResultCode;
import com.printclub.common.util.PageUtils;
import com.printclub.module.artwork.dto.ArtworkCreateDTO;
import com.printclub.module.artwork.dto.ArtworkQuery;
import com.printclub.module.artwork.dto.ArtworkUpdateDTO;
import com.printclub.module.artwork.entity.Artwork;
import com.printclub.module.artwork.mapper.ArtworkMapper;
import com.printclub.module.artwork.service.ArtworkService;
import com.printclub.module.log.service.LogService;
import com.printclub.module.task.entity.PrintTask;
import com.printclub.module.task.mapper.TaskMapper;
import com.printclub.module.user.entity.Member;
import com.printclub.module.user.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 作品库 Service 实现
 *
 * @author F
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArtworkServiceImpl implements ArtworkService {

    private final ArtworkMapper artworkMapper;
    private final TaskMapper taskMapper;
    private final LogService logService;
    private final MemberMapper memberMapper;

    @Override
    public PageResult<Artwork> list(ArtworkQuery query) {
        Page<Artwork> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Artwork> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getAuthorId())) {
            wrapper.eq(Artwork::getAuthorId, query.getAuthorId());
        }
        if (query.getIsRecommended() != null) {
            wrapper.eq(Artwork::getIsRecommended, query.getIsRecommended());
        }
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(Artwork::getArtworkName, query.getKeyword())
                    .or().like(Artwork::getAuthorId, query.getKeyword()));
        }
        // 排序
        applySort(wrapper, query.getSortBy());
        Page<Artwork> result = artworkMapper.selectPage(page, wrapper);
        PageResult<Artwork> pr = PageUtils.toResult(result);
        fillArtworkAuthorNames(pr.getList());
        return pr;
    }

    @Override
    public PageResult<Artwork> myArtworks(ArtworkQuery query) {
        // 不再覆盖 authorId！由 controller 在调用前 setAuthorId(SecurityContext.getCurrentUserId())
        Page<Artwork> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Artwork> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getAuthorId())) {
            wrapper.eq(Artwork::getAuthorId, query.getAuthorId());
        }
        applySort(wrapper, query.getSortBy());
        Page<Artwork> result = artworkMapper.selectPage(page, wrapper);
        PageResult<Artwork> pr = PageUtils.toResult(result);
        fillArtworkAuthorNames(pr.getList());
        return pr;
    }

    @Override
    public Artwork detail(Integer artworkId) {
        Artwork artwork = artworkMapper.selectById(artworkId);
        if (artwork == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "作品不存在");
        }
        // 访问 +1
        artwork.setViewCount(artwork.getViewCount() == null ? 1 : artwork.getViewCount() + 1);
        artworkMapper.updateById(artwork);
        fillArtworkAuthorNames(java.util.Collections.singletonList(artwork));
        return artwork;
    }

    @Override
    public Integer create(ArtworkCreateDTO dto, String currentUserId) {
        // 1. 校验 task 存在 + 属于当前用户 + 已完结（或已取件）
        PrintTask task = taskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "关联任务不存在");
        }
        if (!task.getApplicantId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能登记自己的任务");
        }
        // ✅ v2.2 修复：用户反馈 "选了关联任务但提示只能从已完结的任务登记"
        //    原因：v2 pickup() 把 status 从 DONE(5) 改成 PICKED_UP(8)，老代码 magic number 5 漏了 PICKED_UP
        //    修复：用 PrintTask 常量替代 magic number，且同时允许 DONE + PICKED_UP
        if (task.getStatus() == null
                || (task.getStatus() != PrintTask.STATUS_DONE
                    && task.getStatus() != PrintTask.STATUS_PICKED_UP)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能从已完结的任务登记作品");
        }
        // 2. 校验该任务还没登记过（task_id UNIQUE）
        Long count = artworkMapper.selectCount(
                new LambdaQueryWrapper<Artwork>().eq(Artwork::getTaskId, dto.getTaskId()));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该任务已登记过作品");
        }

        // 3. 构造作品
        Artwork artwork = new Artwork();
        artwork.setTaskId(dto.getTaskId());
        artwork.setAuthorId(currentUserId);
        // 作品名优先级：用户填写 > 任务标题 > "未命名作品"
        String name = dto.getArtworkName();
        if (StrUtil.isBlank(name)) {
            name = StrUtil.isBlank(task.getTitle()) ? "未命名作品" : task.getTitle();
        }
        artwork.setArtworkName(name);
        artwork.setPreviewImage(dto.getPreviewImage());
        artwork.setFinishPhotos(dto.getFinishPhotos());
        artwork.setExperience(dto.getExperience());
        artwork.setIsRecommended(0);
        artwork.setViewCount(0);
        artworkMapper.insert(artwork);
        log.info("作品创建成功：artworkId={}, taskId={}, author={}",
                artwork.getArtworkId(), dto.getTaskId(), currentUserId);
        logService.recordCurrent("artwork.create", "artwork", String.valueOf(artwork.getArtworkId()),
                "登记作品：「" + name + "」（任务 " + dto.getTaskId() + "）");
        return artwork.getArtworkId();
    }

    @Override
    public void update(Integer artworkId, ArtworkUpdateDTO dto, String currentUserId) {
        Artwork artwork = artworkMapper.selectById(artworkId);
        if (artwork == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "作品不存在");
        }
        if (!artwork.getAuthorId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能修改自己的作品");
        }
        if (StrUtil.isNotBlank(dto.getArtworkName())) {
            artwork.setArtworkName(dto.getArtworkName());
        }
        if (dto.getPreviewImage() != null) {
            artwork.setPreviewImage(dto.getPreviewImage());
        }
        if (dto.getFinishPhotos() != null) {
            artwork.setFinishPhotos(dto.getFinishPhotos());
        }
        if (dto.getExperience() != null) {
            artwork.setExperience(dto.getExperience());
        }
        artworkMapper.updateById(artwork);
        log.info("更新作品：artworkId={}, by={}", artworkId, currentUserId);
        logService.recordCurrent("artwork.update", "artwork", String.valueOf(artworkId),
                "编辑作品：「" + artwork.getArtworkName() + "」");
    }

    @Override
    public void setRecommended(Integer artworkId, Integer isRecommended) {
        Artwork artwork = artworkMapper.selectById(artworkId);
        if (artwork == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "作品不存在");
        }
        artwork.setIsRecommended(isRecommended == null ? 0 : (isRecommended == 1 ? 1 : 0));
        artworkMapper.updateById(artwork);
        log.info("设置推荐：artworkId={}, isRecommended={}", artworkId, isRecommended);
        logService.recordCurrent("artwork.setRecommended", "artwork", String.valueOf(artworkId),
                (isRecommended != null && isRecommended == 1 ? "推荐" : "取消推荐") + "：「" + artwork.getArtworkName() + "」");
    }

    @Override
    public void delete(Integer artworkId, String currentUserId) {
        Artwork artwork = artworkMapper.selectById(artworkId);
        if (artwork == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "作品不存在");
        }
        if (!artwork.getAuthorId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己的作品");
        }
        if (artwork.getIsRecommended() != null && artwork.getIsRecommended() == 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "推荐作品不可删除，请先取消推荐");
        }
        artworkMapper.deleteById(artworkId);
        log.info("删除作品：artworkId={}, by={}", artworkId, currentUserId);
        logService.recordCurrent("artwork.delete", "artwork", String.valueOf(artworkId),
                "删除作品：「" + artwork.getArtworkName() + "」");
    }

    @Override
    public PageResult<Artwork> recommended(int limit) {
        Page<Artwork> page = new Page<>(1, limit);
        LambdaQueryWrapper<Artwork> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Artwork::getIsRecommended, 1)
                .orderByDesc(Artwork::getViewCount)
                .orderByDesc(Artwork::getCreateTime);
        Page<Artwork> result = artworkMapper.selectPage(page, wrapper);
        PageResult<Artwork> pr = PageUtils.toResult(result);
        fillArtworkAuthorNames(pr.getList());
        return pr;
    }

    // ============== 私有方法 ==============

    /**
     * 批量把 authorId 翻译成 authorName（一次查 member，N+1 → 1 次）
     * v2 重构：直接用 memberMapper.selectIdNameMap 公共方法
     */
    private void fillArtworkAuthorNames(List<Artwork> artworks) {
        if (artworks == null || artworks.isEmpty()) return;
        Set<String> authorIds = new HashSet<>();
        for (Artwork a : artworks) {
            if (a.getAuthorId() != null) authorIds.add(a.getAuthorId());
        }
        // 用 memberMapper.selectIdNameMap 公共方法（替代原本复制 task 的 selectBatchIds 循环）
        Map<String, String> id2name = memberMapper.selectIdNameMap(authorIds);
        for (Artwork a : artworks) {
            a.setAuthorName(id2name.get(a.getAuthorId()));
        }
    }

    private void applySort(LambdaQueryWrapper<Artwork> wrapper, String sortBy) {
        if (sortBy == null) sortBy = "latest";
        switch (sortBy) {
            case "hottest":
                wrapper.orderByDesc(Artwork::getViewCount);
                break;
            case "recommended":
                wrapper.orderByDesc(Artwork::getIsRecommended)
                        .orderByDesc(Artwork::getViewCount);
                break;
            case "latest":
            default:
                wrapper.orderByDesc(Artwork::getCreateTime);
                break;
        }
    }
}
