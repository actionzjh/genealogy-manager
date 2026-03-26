package com.genealogy.controller;

import com.genealogy.entity.FamilyPhoto;
import com.genealogy.service.FamilyPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家族相册控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/family-photo")
@RequiredArgsConstructor
public class FamilyPhotoController {

    private final FamilyPhotoService photoService;

    /**
     * 获取某家谱公开相册照片
     */
    @GetMapping("/public/{genealogyId}")
    public ResponseEntity<Map<String, Object>> listPublic(@PathVariable Long genealogyId) {
        Map<String, Object> result = new HashMap<>();
        List<FamilyPhoto> photos = photoService.listPublic(genealogyId);
        result.put("success", true);
        result.put("data", photos);
        return ResponseEntity.ok(result);
    }

    /**
     * 管理端列表
     */
    @GetMapping("/list/{genealogyId}")
    public ResponseEntity<Map<String, Object>> listAll(
            @PathVariable Long genealogyId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = new HashMap<>();
        Page<FamilyPhoto> data = photoService.listAll(genealogyId, page, size);
        result.put("success", true);
        result.put("data", data.getContent());
        result.put("total", data.getTotalElements());
        result.put("totalPages", data.getTotalPages());
        return ResponseEntity.ok(result);
    }

    /**
     * 添加照片（需要登录，管理员权限）
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody FamilyPhoto photo,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        FamilyPhotoService.Result createResult = photoService.create(photo, userId);
        result.put("success", createResult.isSuccess());
        result.put("message", createResult.getMessage());
        result.put("data", createResult.getData());
        return ResponseEntity.ok(result);
    }

    /**
     * 更新照片
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody FamilyPhoto photo,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        FamilyPhotoService.Result updateResult = photoService.update(photo, userId);
        result.put("success", updateResult.isSuccess());
        result.put("message", updateResult.getMessage());
        result.put("data", updateResult.getData());
        return ResponseEntity.ok(result);
    }

    /**
     * 删除照片
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        FamilyPhotoService.Result deleteResult = photoService.delete(id, userId);
        result.put("success", deleteResult.isSuccess());
        result.put("message", deleteResult.getMessage());
        return ResponseEntity.ok(result);
    }
}
