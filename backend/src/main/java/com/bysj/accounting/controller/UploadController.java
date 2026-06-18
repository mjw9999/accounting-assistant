package com.bysj.accounting.controller;

import com.bysj.accounting.common.ApiResponse;
import com.bysj.accounting.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
@SuppressWarnings("null")
public class UploadController {
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> avatar(@RequestPart("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("只支�?jpg、png、gif、webp 格式头像");
        }
        String day = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Path folder = Paths.get(uploadDir, "avatars", day).toAbsolutePath().normalize();
        Files.createDirectories(folder);
        String filename = UUID.randomUUID() + "." + ext;
        Path target = folder.resolve(filename);
        file.transferTo(target);
        String url = "/uploads/avatars/" + day + "/" + filename;
        return ApiResponse.ok("上传成功", Map.of("url", url));
    }
}
