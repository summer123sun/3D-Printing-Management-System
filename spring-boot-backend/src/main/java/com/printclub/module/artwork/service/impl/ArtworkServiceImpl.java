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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return PageUtils.toResult(result);
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
        return PageUtils.toResult(result);
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
        return artwork;
    }

    @Override
    public Integer create(ArtworkCreateDTO dto, String currentUserId) {
        // 1. 校验 task 存在 + 属于当前用户 + 已完结
        PrintTask task = taskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "关联任务不存在");
        }
        if (!task.getApplicantId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能登记自己的任务");
        }
        if (task.getStatus() == null || task.getStatus() != 5) {
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
        return PageUtils.toResult(result);
    }

    // ============== 私有方法 ==============

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
