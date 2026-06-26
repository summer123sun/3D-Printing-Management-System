package com.printclub.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 文件上传工具
 *
 * <p>存储路径：{@code <upload-dir>/<类型>/<yyyyMM>/<uuid>.<ext>}</p>
 * <p>返回 URL：{@code <access-prefix><类型>/<yyyyMM>/<uuid>.<ext>}</p>
 *
 * @author D
 */
@Slf4j
@Component
public class FileUploadUtil {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-prefix}")
    private String accessPrefix;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 保存文件
     *
     * @param file 上传的文件
     * @param type 业务类型（stl / img / project 等），用于分目录
     * @return 可访问的 URL（前端拼域名使用）
     */
    public String save(MultipartFile file, String type) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        // 1. 文件名：uuid + 原扩展名
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
                : "";

        String fileName = IdUtil.fastSimpleUUID() + (ext.isEmpty() ? "" : "." + ext);

        // 2. 路径：<upload-dir>/<type>/<yyyyMM>/<fileName>
        String monthDir = LocalDate.now().format(MONTH_FMT);
        String relativePath = type + "/" + monthDir + "/" + fileName;
        File dest = new File(uploadDir, relativePath);

        // 3. 兜底：绝对化路径 + 自动创建父目录（解决相对路径 + 路径不存在问题）
        Path destPath = Paths.get(dest.toURI());
        File parent = destPath.getParent().toFile();
        if (!parent.exists()) {
            FileUtil.mkParentDirs(dest);
            log.info("创建上传父目录：{}", parent.getAbsolutePath());
        }

        // 4. 用 Files.copy 流复制（不依赖 multipart temp 文件，避免 transferTo 的 FileNotFoundException）
        try (var in = file.getInputStream()) {
            Files.copy(in, destPath, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("文件保存成功：{}", dest.getAbsolutePath());

        // 5. 返回可访问 URL
        return accessPrefix + relativePath;
    }

    /**
     * 删除文件
     */
    public void delete(String url) {
        if (url == null || !url.startsWith(accessPrefix)) return;
        String relativePath = url.substring(accessPrefix.length());
        File file = new File(uploadDir, relativePath);
        if (file.exists() && file.delete()) {
            log.info("文件删除：{}", file.getAbsolutePath());
        }
    }
}