package com.printclub.module.file.controller;

import com.printclub.common.annotation.RequireAuth;
import com.printclub.common.annotation.RequireRole;
import com.printclub.common.exception.BusinessException;
import com.printclub.common.result.Result;
import com.printclub.common.result.ResultCode;
import com.printclub.common.util.FileUploadUtil;
import com.printclub.common.util.SecurityContext;
import com.printclub.module.log.service.LogService;
import com.printclub.module.user.entity.Member;
import com.printclub.module.user.mapper.MemberMapper;
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
import java.util.ArrayList;
import java.util.Comparator;
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
    // ✅ v2.2 round 5 修复（用户反馈）：删除文件不写日志
    //    修复：注入 logService + memberMapper（查真实姓名）
    private final LogService logService;
    private final MemberMapper memberMapper;

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
     * ✅ v2.2 修复（用户反馈）：管理员列出所有已上传文件
     * GET /api/file/list?type=stl  （type 可选：stl / img / project；不传 = 全部）
     *
     * ✅ v2.2 round 4 修复：FileUploadUtil.save() 把文件存到 <type>/<yyyyMM>/<fileName>，
     *    之前 list 只扫描 <type>/ 这一层，漏了 <yyyyMM>/ 子目录里所有文件
     *    修复：用 walk 递归扫描，返回完整相对路径（stl/202607/xxx.stl）
     */
    @Operation(summary = "管理员列出所有文件（仅 PRESIDENT/TECH_LEAD）")
    @GetMapping("/list")
    @RequireRole({1, 2})
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String type) {
        File root = new File(uploadDir);
        if (!root.exists() || !root.isDirectory()) {
            return Result.success(new ArrayList<>());
        }
        List<Map<String, Object>> out = new ArrayList<>();
        // 起点目录：传 type 就在 <type>/ 下递归；不传就在 upload 根目录下递归
        File[] startDirs;
        if (type != null && !type.isBlank()) {
            File d = new File(root, type);
            startDirs = d.exists() ? new File[]{d} : new File[0];
        } else {
            startDirs = root.listFiles(File::isDirectory);
            if (startDirs == null) startDirs = new File[0];
        }
        for (File typeDir : startDirs) {
            walkAndCollect(typeDir, typeDir.getName(), out);
        }
        // 按修改时间倒序
        out.sort(Comparator.comparingLong((Map<String, Object> m) -> ((Number) m.get("lastModified")).longValue()).reversed());
        return Result.success(out);
    }

    /**
     * 递归扫描目录下所有文件（最多 2 层：type/yyyyMM/file.ext）
     */
    private void walkAndCollect(File dir, String typeName, List<Map<String, Object>> out) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isFile()) {
                Map<String, Object> entry = new HashMap<>();
                // 相对 uploadDir 的路径
                String relativePath = dir.getName().equals(typeName)
                        ? typeName + "/" + f.getName()
                        : typeName + "/" + dir.getName() + "/" + f.getName();
                entry.put("name", f.getName());
                entry.put("type", typeName);
                entry.put("path", relativePath);
                entry.put("size", f.length());
                entry.put("lastModified", f.lastModified());
                out.add(entry);
            } else if (f.isDirectory()) {
                // 递归子目录（一般是 yyyyMM 月份目录）
                walkAndCollect(f, typeName, out);
            }
        }
    }

    /**
     * 文件删除（DELETE /api/file/**）
     * ✅ v2.2 修复：现在 @RequireAuth 任何登录用户都能删自己的文件
     *    管理员想删任意文件用下面的 /admin 接口
     * ✅ v2.2 round 5 修复：删除文件要写日志（用户反馈删了没记录）
     */
    @Operation(summary = "文件删除（用户自己的）")
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
        // 写审计日志
        recordFileLog("file.delete", relativePath, "删除文件");
        return Result.success();
    }

    /**
     * ✅ v2.2 新增：管理员强制删除任意文件
     * DELETE /api/file/admin?path=stl/xxx.stl
     */
    @Operation(summary = "管理员删除任意文件（仅 PRESIDENT/TECH_LEAD）")
    @DeleteMapping("/admin")
    @RequireRole({1, 2})
    public Result<Void> adminDelete(@RequestParam String path) {
        if (path == null || path.isBlank() || path.contains("..")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法路径");
        }
        File file = new File(uploadDir, path);
        if (!file.exists()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }
        if (!file.delete()) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "删除失败");
        }
        log.info("管理员删除文件：{}", file.getAbsolutePath());
        recordFileLog("file.adminDelete", path, "管理员强制删除文件");
        return Result.success();
    }

    /**
     * 写文件操作的审计日志（用 recordAs 显式传 userId + 真实姓名，因为 SecurityContext 可能在异步场景拿不到）
     */
    private void recordFileLog(String operation, String path, String desc) {
        try {
            String userId = SecurityContext.getCurrentUserId();
            String username = userId;
            if (userId != null) {
                try {
                    Member m = memberMapper.selectById(userId);
                    if (m != null && m.getName() != null) {
                        username = m.getName();
                    }
                } catch (Exception ignore) {}
            }
            logService.recordAs(userId, username, operation, "file", path, desc + "：" + path);
        } catch (Exception e) {
            log.warn("记录文件操作日志失败：op={}, path={}", operation, path, e);
        }
    }
}
