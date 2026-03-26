package com.genealogy.controller;

import com.genealogy.entity.FamilyActivity;
import com.genealogy.service.FamilyActivityService;
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
 * 家族活动控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class FamilyActivityController {

    private final FamilyActivityService activityService;

    /**
     * 创建活动
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody FamilyActivity activity,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        FamilyActivityService.Result serviceResult = activityService.create(activity, userId);
        result.put("success", serviceResult.isSuccess());
        if (serviceResult.isSuccess()) {
            result.put("data", serviceResult.getData());
            result.put("message", "创建成功");
        } else {
            result.put("message", serviceResult.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody FamilyActivity activity,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        FamilyActivityService.Result serviceResult = activityService.update(id, activity, userId);
        result.put("success", serviceResult.isSuccess());
        result.put("message", serviceResult.isSuccess() ? "更新成功" : serviceResult.getMessage());
        if (serviceResult.isSuccess()) {
            result.put("data", serviceResult.getData());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        FamilyActivityService.Result serviceResult = activityService.delete(id, userId);
        result.put("success", serviceResult.isSuccess());
        result.put("message", serviceResult.isSuccess() ? "删除成功" : serviceResult.getMessage());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取公开活动列表（分页）
     */
    @GetMapping("/public/{genealogyId}")
    public ResponseEntity<Map<String, Object>> listPublic(
            @PathVariable Long genealogyId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new HashMap<>();
        Page<FamilyActivity> data = activityService.listPublic(genealogyId, page, size);
        result.put("success", true);
        result.put("data", data.getContent());
        result.put("total", data.getTotalElements());
        result.put("totalPages", data.getTotalPages());
        result.put("currentPage", data.getNumber() + 1);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有活动列表（管理后台用）
     */
    @GetMapping("/list/{genealogyId}")
    public ResponseEntity<Map<String, Object>> listAll(
            @PathVariable Long genealogyId,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        List<FamilyActivity> data = activityService.listAll(genealogyId);
        result.put("success", true);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        var opt = activityService.getById(id);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "活动不存在");
            return ResponseEntity.ok(result);
        }
        result.put("success", true);
        result.put("data", opt.get());
        return ResponseEntity.ok(result);
    }
}
