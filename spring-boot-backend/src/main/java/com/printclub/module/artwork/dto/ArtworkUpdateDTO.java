package com.printclub.module.artwork.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 作品心得更新 DTO（用户给作品补心得/照片/封面）
 */
@Data
public class ArtworkUpdateDTO {

    @Size(max = 100)
    private String artworkName;

    /** 封面图 URL — v2.14 加 @Pattern 校验（防存储型 XSS） */
    @Pattern(regexp = "^(/uploads/|https?://).+$",
             message = "封面图 URL 必须以 /uploads/ 或 http(s):// 开头")
    @Size(max = 255)
    private String previewImage;

    /** 成品照片（多图，逗号分隔）— v2.14 加 @Pattern 校验 */
    @Pattern(regexp = "^((/uploads/|https?://)[^,]+)(,((/uploads/|https?://)[^,]+))*$",
             message = "成品照片 URL 必须以 /uploads/ 或 http(s):// 开头，多图逗号分隔")
    @Size(max = 2000)
    private String finishPhotos;

    /** 心得总结 */
    @Size(max = 5000, message = "心得总结最多 5000 字")
    private String experience;
}
