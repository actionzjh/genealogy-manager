package com.genealogy.controller;

import com.genealogy.entity.UserFavorite;
import com.genealogy.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户收藏关注控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> add(
            @RequestParam String type,
            @RequestParam Long targetId,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        UserFavoriteService.Result serviceResult = favoriteService.add(userId, type, targetId);
        result.put("success", serviceResult.isSuccess());
        result.put("message", serviceResult.getMessage());
        result.put("data", serviceResult.getData());
        return ResponseEntity.ok(result);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> remove(
            @RequestParam Long targetId,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        UserFavoriteService.Result serviceResult = favoriteService.remove(userId, targetId);
        result.put("success", serviceResult.isSuccess());
        result.put("message", serviceResult.getMessage());
        return ResponseEntity.ok(result);
    }

    /**
     * 查询用户收藏列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam String type,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        List<UserFavorite> list = favoriteService.list(userId, type);
        result.put("success", true);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> check(
            @RequestParam Long targetId,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        boolean favorited = favoriteService.isFavorite(userId, targetId);
        result.put("success", true);
        result.put("favorited", favorited);
        return ResponseEntity.ok(result);
    }
}
