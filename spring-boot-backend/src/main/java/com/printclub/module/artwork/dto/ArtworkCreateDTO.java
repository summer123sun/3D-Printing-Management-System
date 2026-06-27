package com.printclub.module.artwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 作品创建 DTO
 *
 * <p>用户从已完结的打印任务"登记"为作品：</p>
 * <ul>
 *   <li>taskId：必须是当前用户的已完结任务（status=5）</li>
 *   <li>artworkName：作品名（默认跟 task.title）</li>
 *   <li>previewImage：封面图 URL（可选）</li>
 *   <li>finishPhotos：成品照片 URL（多图逗号分隔）</li>
 *   <li>experience：心得总结</li>
 * </ul>
 */
@Data
public class ArtworkCreateDTO {

    /** 关联任务 ID（必须已完结 + 属于当前用户 + 尚未登记） */
    @NotBlank(message = "请选择关联任务")
    @Size(max = 20)
    private String taskId;

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
