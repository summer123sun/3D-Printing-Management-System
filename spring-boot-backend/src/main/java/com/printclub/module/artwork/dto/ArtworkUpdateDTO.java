package com.printclub.module.artwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 作品心得更新 DTO（用户给作品补心得/照片）
 */
@Data
public class ArtworkUpdateDTO {

    @Size(max = 100)
    private String artworkName;

    /** 成品照片（多图，逗号分隔） */
    @Size(max = 500)
    private String finishPhotos;

    /** 心得总结 */
    private String experience;
}
