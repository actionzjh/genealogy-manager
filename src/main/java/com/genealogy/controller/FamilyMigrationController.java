package com.genealogy.controller;

import com.genealogy.entity.FamilyMigration;
import com.genealogy.service.FamilyMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家族迁徙地图控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/family-migration")
@RequiredArgsConstructor
public class FamilyMigrationController {

    private final FamilyMigrationService migrationService;

    /**
     * 获取家谱公开迁徙记录
     */
    @GetMapping("/public/{genealogyId}")
    public ResponseEntity<Map<String, Object>> listPublic(@PathVariable Long genealogyId) {
        Map<String, Object> result = new HashMap<>();
        List<FamilyMigration> data = migrationService.listPublic(genealogyId);
        result.put("success", true);
        result.put("data", data);
        result.put("total", data.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 后台管理 - 获取列表
     */
    @GetMapping("/list/{genealogyId}")
    public ResponseEntity<Map<String, Object>> listAll(
            @PathVariable Long genealogyId,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        // 简化：这里权限在service层判断
        List<FamilyMigration> data = migrationService.listPublic(genealogyId);
        result.put("success", true);
        result.put("data", data);
        result.put("total", data.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 添加迁徙记录
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody FamilyMigration migration,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        FamilyMigrationService.Result serviceResult = migrationService.create(migration, userId);
        result.put("success", serviceResult.isSuccess());
        result.put("message", serviceResult.getMessage());
        result.put("data", serviceResult.getData());
        return ResponseEntity.ok(result);
    }

    /**
     * 更新迁徙记录
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody FamilyMigration migration,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        FamilyMigrationService.Result serviceResult = migrationService.update(migration, userId);
        result.put("success", serviceResult.isSuccess());
        result.put("message", serviceResult.getMessage());
        result.put("data", serviceResult.getData());
        return ResponseEntity.ok(result);
    }

    /**
     * 删除迁徙记录
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
        FamilyMigrationService.Result serviceResult = migrationService.delete(id, userId);
        result.put("success", serviceResult.isSuccess());
        result.put("message", serviceResult.getMessage());
        return ResponseEntity.ok(result);
    }
}
