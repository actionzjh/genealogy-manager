package com.genealogy.controller;

import com.genealogy.entity.FamilyCelebrity;
import com.genealogy.service.FamilyCelebrityService;
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
 * 家族名人控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/family-celebrity")
@RequiredArgsConstructor
public class FamilyCelebrityController {

    private final FamilyCelebrityService celebrityService;

    /**
     * 获取公开名人列表（公开接口）
     */
    @GetMapping("/public/{genealogyId}")
    public ResponseEntity<Map<String, Object>> listPublic(@PathVariable Long genealogyId) {
        Map<String, Object> result = new HashMap<>();
        List<FamilyCelebrity> data = celebrityService.listPublic(genealogyId);
        result.put("success", true);
        result.put("data", data);
        result.put("total", data.size());
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
        Page<FamilyCelebrity> data = celebrityService.listAll(genealogyId, page, size);
        result.put("success", true);
        result.put("data", data.getContent());
        result.put("total", data.getTotalElements());
        result.put("totalPages", data.getTotalPages());
        return ResponseEntity.ok(result);
    }

    /**
     * 添加名人
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody FamilyCelebrity celebrity,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        FamilyCelebrityService.Result createResult = celebrityService.create(celebrity, userId);
        result.put("success", createResult.isSuccess());
        result.put("message", createResult.getMessage());
        result.put("data", createResult.getData());
        return ResponseEntity.ok(result);
    }

    /**
     * 更新名人
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody FamilyCelebrity celebrity,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        FamilyCelebrityService.Result updateResult = celebrityService.update(celebrity, userId);
        result.put("success", updateResult.isSuccess());
        result.put("message", updateResult.getMessage());
        result.put("data", updateResult.getData());
        return ResponseEntity.ok(result);
    }

    /**
     * 删除名人
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
        FamilyCelebrityService.Result deleteResult = celebrityService.delete(id, userId);
        result.put("success", deleteResult.isSuccess());
        result.put("message", deleteResult.getMessage());
        return ResponseEntity.ok(result);
    }
}
