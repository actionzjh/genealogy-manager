package com.genealogy.controller;

import com.genealogy.entity.RootSearch;
import com.genealogy.service.RootSearchService;
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
 * 寻根匹配控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/root-search")
@RequiredArgsConstructor
public class RootSearchController {

    private final RootSearchService rootSearchService;

    /**
     * 发布寻根启事
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody RootSearch search, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        RootSearchService.Result serviceResult = rootSearchService.create(search, userId);
        result.put("success", serviceResult.isSuccess());
        if (serviceResult.isSuccess()) {
            result.put("data", serviceResult.getData());
            result.put("message", "发布成功");
        } else {
            result.put("message", serviceResult.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 更新状态（标记已找到/开启
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        RootSearchService.Result serviceResult = rootSearchService.updateStatus(id, userId, status);
        result.put("success", serviceResult.isSuccess());
        result.put("message", serviceResult.isSuccess() ? "更新成功" : serviceResult.getMessage());
        if (serviceResult.isSuccess()) {
            result.put("data", serviceResult.getData());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 删除
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
        RootSearchService.Result serviceResult = rootSearchService.delete(id, userId);
        result.put("success", serviceResult.isSuccess());
        result.put("message", serviceResult.isSuccess() ? "删除成功" : serviceResult.getMessage());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取公开寻根列表（分页）
     */
    @GetMapping("/public/list")
    public ResponseEntity<Map<String, Object>> listPublic(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new HashMap<>();
        Page<RootSearch> data = rootSearchService.listPublic(page, size);
        result.put("success", true);
        result.put("data", data.getContent());
        result.put("total", data.getTotalElements());
        result.put("totalPages", data.getTotalPages());
        result.put("currentPage", data.getNumber() + 1);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取我发布的寻根列表
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> listMy(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        List<RootSearch> data = rootSearchService.listMy(userId);
        result.put("success", true);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    /**
     * 智能匹配
     */
    @GetMapping("/match/{id}")
    public ResponseEntity<Map<String, Object>> match(@PathVariable Long id, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        // 获取寻根启事
        java.util.Optional<RootSearch> opt = rootSearchService.findById(id);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "寻根启事不存在");
            return ResponseEntity.ok(result);
        }
        List<RootSearchService.MatchResult> matches = rootSearchService.match(opt.get());
        result.put("success", true);
        result.put("data", matches);
        result.put("search", opt.get());
        return ResponseEntity.ok(result);
    }

    /**
     * 对任意寻根条件即时匹配（不发布也可以先匹配）
     */
    @PostMapping("/match-direct")
    public ResponseEntity<Map<String, Object>> matchDirect(@RequestBody RootSearch search, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        List<RootSearchService.MatchResult> matches = rootSearchService.match(search);
        result.put("success", true);
        result.put("data", matches);
        return ResponseEntity.ok(result);
    }
}
