package com.printclub.module.artwork.service;

import com.printclub.common.result.PageResult;
import com.printclub.module.artwork.dto.ArtworkQuery;
import com.printclub.module.artwork.dto.ArtworkUpdateDTO;
import com.printclub.module.artwork.entity.Artwork;

/**
 * 作品库 Service
 *
 * @author F
 */
public interface ArtworkService {

    /** 作品列表（分页 + 多维筛选） */
    PageResult<Artwork> list(ArtworkQuery query);

    /** 我的作品（学号 = 当前用户） */
    PageResult<Artwork> myArtworks(ArtworkQuery query);

    /** 作品详情（访问时 view_count +1） */
    Artwork detail(Integer artworkId);

    /** 从已完结任务创建作品 */
    Integer create(com.printclub.module.artwork.dto.ArtworkCreateDTO dto, String currentUserId);

    /** 更新作品（仅作者本人，限定：作品名/成品照/心得） */
    void update(Integer artworkId, ArtworkUpdateDTO dto, String currentUserId);

    /** 设置推荐/取消推荐（仅 STAFF+） */
    void setRecommended(Integer artworkId, Integer isRecommended);

    /** 删除作品（仅作者本人 + 仅当 is_recommended=0） */
    void delete(Integer artworkId, String currentUserId);

    /** 推荐作品列表（is_recommended=1 + 按 view_count 降序） */
    PageResult<Artwork> recommended(int limit);
}
