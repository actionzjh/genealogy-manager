package com.genealogy.controller;

import com.genealogy.service.CloudStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 图片上传控制器 - 上传到云存储
 */
@Slf4j
@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageUploadController {

    private final CloudStorageService cloudStorageService;

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            MultipartFile file,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }

        if (file == null || file.isEmpty()) {
            result.put("success", false);
            result.put("message", "请选择文件");
            return ResponseEntity.ok(result);
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            result.put("success", false);
            result.put("message", "只能上传图片文件");
            return ResponseEntity.ok(result);
        }

        // 检查文件大小 (限制 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            result.put("success", false);
            result.put("message", "图片大小不能超过 5MB");
            return ResponseEntity.ok(result);
        }

        CloudStorageService.UploadResult uploadResult = cloudStorageService.uploadImage(file);
        result.put("success", uploadResult.isSuccess());
        result.put("message", uploadResult.getMessage());
        result.put("url", uploadResult.getUrl());
        result.put("fileKey", uploadResult.getFileKey());
        return ResponseEntity.ok(result);
    }
}
