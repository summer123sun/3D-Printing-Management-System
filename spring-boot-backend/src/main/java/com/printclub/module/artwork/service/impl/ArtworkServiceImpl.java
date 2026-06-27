package com.printclub.module.artwork.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printclub.common.exception.BusinessException;
import com.printclub.common.result.PageResult;
import com.printclub.common.result.ResultCode;
import com.printclub.common.util.PageUtils;
import com.printclub.module.artwork.dto.ArtworkQuery;
import com.printclub.module.artwork.dto.ArtworkUpdateDTO;
import com.printclub.module.artwork.entity.Artwork;
import com.printclub.module.artwork.mapper.ArtworkMapper;
import com.printclub.module.artwork.service.ArtworkService;
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
        query.setAuthorId(null); // 由 service 设置
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
        if (dto.getFinishPhotos() != null) {
            artwork.setFinishPhotos(dto.getFinishPhotos());
        }
        if (dto.getExperience() != null) {
            artwork.setExperience(dto.getExperience());
        }
        artworkMapper.updateById(artwork);
        log.info("更新作品：artworkId={}, by={}", artworkId, currentUserId);
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
