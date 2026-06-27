package com.printclub.module.artwork.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 作品心得更新 DTO（用户给作品补心得/照片/封面）
 */
@Data
public class ArtworkUpdateDTO {

    @Size(max = 100)
    private String artworkName;

    /** 封面图 URL */
    @Size(max = 255)
    private String previewImage;

    /** 成品照片（多图，逗号分隔） */
    @Size(max = 2000)
    private String finishPhotos;

    /** 心得总结 */
    private String experience;
}
