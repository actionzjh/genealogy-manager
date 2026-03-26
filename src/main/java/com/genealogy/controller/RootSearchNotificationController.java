package com.genealogy.controller;

import com.genealogy.entity.RootSearchNotification;
import com.genealogy.service.RootSearchNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 寻根通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/root-search-notification")
@RequiredArgsConstructor
public class RootSearchNotificationController {

    private final RootSearchNotificationService notificationService;

    /**
     * 获取用户未读通知
     */
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnread(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        List<RootSearchNotification> notifications = notificationService.getUnread(userId);
        long unreadCount = notificationService.countUnread(userId);
        result.put("success", true);
        result.put("data", notifications);
        result.put("unreadCount", unreadCount);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户所有通知
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAll(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        List<RootSearchNotification> notifications = notificationService.getAll(userId);
        result.put("success", true);
        result.put("data", notifications);
        return ResponseEntity.ok(result);
    }

    /**
     * 标记单个通知已读
     */
    @PostMapping("/read/{id}")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAsRead(id, userId);
        result.put("success", true);
        result.put("message", "标记已读成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 标记所有已读
     */
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAllAsRead(userId);
        result.put("success", true);
        result.put("message", "标记全部已读成功");
        return ResponseEntity.ok(result);
    }
}
