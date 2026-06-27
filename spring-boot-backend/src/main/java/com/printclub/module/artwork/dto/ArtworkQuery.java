package com.printclub.module.artwork.dto;

import lombok.Data;

/**
 * 作品查询条件
 */
@Data
public class ArtworkQuery {

    /** 第几页 */
    private Integer page = 1;

    /** 每页大小 */
    private Integer size = 20;

    /** 按作者学号筛选 */
    private String authorId;

    /** 按是否推荐筛选：0/1/null（全部） */
    private Integer isRecommended;

    /** 关键字（按作品名/作者名模糊搜索） */
    private String keyword;

    /** 排序：latest / hottest / recommended */
    private String sortBy = "latest";
}
