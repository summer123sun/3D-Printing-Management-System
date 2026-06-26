package com.printclub.module.artwork.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作品库实体（对应 artwork 表）
 *
 * @author F
 */
@Data
@TableName("artwork")
public class Artwork implements Serializable {

    @TableId(value = "artwork_id", type = IdType.AUTO)
    private Integer artworkId;

    @TableField("task_id")
    private String taskId;

    @TableField("author_id")
    private String authorId;

    @TableField("artwork_name")
    private String artworkName;

    @TableField("preview_image")
    private String previewImage;

    /** 成品照片，多图逗号分隔 */
    @TableField("finish_photos")
    private String finishPhotos;

    /** 心得总结（TEXT） */
    private String experience;

    /** 是否推荐为教学案例 */
    @TableField("is_recommended")
    private Integer isRecommended;

    /** 浏览次数 */
    @TableField("view_count")
    private Integer viewCount;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}