package com.printclub.module.file.controller;

import com.printclub.common.annotation.RequireAuth;
import com.printclub.common.exception.BusinessException;
import com.printclub.common.result.Result;
import com.printclub.common.result.ResultCode;
import com.printclub.common.util.FileUploadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传 Controller
 *
 * <p>支持的类型：stl（STL模型）、img（图片）、project（项目文件）</p>
 *
 * @author A
 */
@Slf4j
@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadUtil fileUploadUtil;

    @Value("${file.upload-dir:./uploads/}")
    private String uploadDir;

    /**
     * 允许上传的 MIME 类型白名单
     */
    private static final Map<String, String> ALLOWED_TYPES = new HashMap<>();

    static {
        // STL 文件
        ALLOWED_TYPES.put("application/sla", "stl");
        ALLOWED_TYPES.put("application/vnd.ms-pki.stl", "stl");
        // 图片
        ALLOWED_TYPES.put("image/jpeg", "img");
        ALLOWED_TYPES.put("image/png", "img");
        ALLOWED_TYPES.put("image/gif", "img");
        ALLOWED_TYPES.put("image/webp", "img");
        // PDF / 文档
        ALLOWED_TYPES.put("application/pdf", "project");
        ALLOWED_TYPES.put("application/msword", "project");
        ALLOWED_TYPES.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "project");
        ALLOWED_TYPES.put("application/zip", "project");
        ALLOWED_TYPES.put("application/x-rar-compressed", "project");
    }

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    @RequireAuth
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }

        // 1. 检查文件类型
        String contentType = file.getContentType();
        String originalName = file.getOriginalFilename();
        String ext = "";

        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        }

        // 2. 确定上传类型目录
        String type = ALLOWED_TYPES.getOrDefault(contentType, null);

        // 如果 MIME 类型不在白名单，尝试通过扩展名判断
        if (type == null) {
            if ("stl".equals(ext)) {
                type = "stl";
            } else if (List.of("jpg", "jpeg", "png", "gif", "webp").contains(ext)) {
                type = "img";
            } else if (List.of("pdf", "doc", "docx", "zip", "rar").contains(ext)) {
                type = "project";
            } else {
                return Result.error(400, "不支持的文件类型：" + ext);
            }
        }

        // 3. 文件大小验证
        long maxSize = switch (type) {
            case "stl" -> 50 * 1024 * 1024;   // 50MB
            case "img" -> 5 * 1024 * 1024;    // 5MB
            case "project" -> 20 * 1024 * 1024; // 20MB
            default -> 10 * 1024 * 1024;
        };
        if (file.getSize() > maxSize) {
            return Result.error(400, "文件大小超过限制（最大" + (maxSize / 1024 / 1024) + "MB）");
        }

        // 4. 保存文件
        try {
            String url = fileUploadUtil.save(file, type);
            log.info("文件上传成功：{} → {}", originalName, url);

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("fileName", originalName);
            result.put("size", String.valueOf(file.getSize()));

            return Result.success(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 文件下载（GET /api/file/download/**）
     * <p>URL 格式：/api/file/download/stl/test.stl（与 upload 返回的 url 对应）</p>
     */
    @Operation(summary = "文件下载")
    @GetMapping("/download/**")
    public ResponseEntity<Resource> download(HttpServletRequest request) {
        // 1. 提取路径（去掉 /api/file/download 前缀）
        String fullPath = (String) request.getAttribute(
                org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String relativePath = fullPath;
        if (fullPath.startsWith("/api/file/download/")) {
            relativePath = fullPath.substring("/api/file/download/".length());
        }
        if (relativePath.contains("..")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法路径");
        }

        // 2. 拼绝对路径 + 安全检查
        File file = new File(uploadDir, relativePath);
        if (!file.exists() || !file.isFile()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }

        // 3. 自动判断 Content-Type
        String contentType;
        try {
            contentType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        // 4. 返回
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    /**
     * 文件删除（DELETE /api/file/**）
     */
    @Operation(summary = "文件删除")
    @DeleteMapping("/**")
    @RequireAuth
    public Result<Void> delete(HttpServletRequest request) {
        String fullPath = (String) request.getAttribute(
                org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String relativePath = fullPath;
        if (fullPath.startsWith("/api/file/")) {
            relativePath = fullPath.substring("/api/file/".length());
        }
        if (relativePath.contains("..") || relativePath.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法路径");
        }

        File file = new File(uploadDir, relativePath);
        if (!file.exists()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }
        if (!file.delete()) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "删除失败");
        }
        log.info("文件已删除：{}", file.getAbsolutePath());
        return Result.success();
    }
}
