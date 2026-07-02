package com.printclub.module.artwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    /**
     * 作品名（DB NOT NULL）
     * ✅ v2.14 修复（审查发现）：DB 要求非空但 DTO 之前没 @NotBlank，
     *    前端 handleSubmit 强校验兜底；如果绕过前端直接调 API，
     *    之前会用 task.title 兜底，但 task.title 也是空就脏数据
     */
    @NotBlank(message = "请输入作品名")
    @Size(max = 100)
    private String artworkName;

    /**
     * 封面图 URL（可选）
     * ✅ v2.14 修复（审查发现）：之前只 @Size(255) 没校验格式，
     *    用户若绕过前端存 `<script>alert(1)</script>` 到 DB（不算 XSS），
     *    但前端若 v-html 渲染会执行 → 存储型 XSS
     *    修法：限制只能以 /uploads/ 或 http(s):// 开头
     *    前端已有 escapeHtml(v2.13)，这里是双保险
     */
    @Pattern(regexp = "^(/uploads/|https?://).+$",
             message = "封面图 URL 必须以 /uploads/ 或 http(s):// 开头")
    @Size(max = 255)
    private String previewImage;

    /** 成品照片（多图，逗号分隔） */
    @Pattern(regexp = "^((/uploads/|https?://)[^,]+)(,((/uploads/|https?://)[^,]+))*$",
             message = "成品照片 URL 必须以 /uploads/ 或 http(s):// 开头，多图逗号分隔")
    @Size(max = 2000)
    private String finishPhotos;

    /** 心得总结 */
    @Size(max = 5000, message = "心得总结最多 5000 字")
    private String experience;
}
