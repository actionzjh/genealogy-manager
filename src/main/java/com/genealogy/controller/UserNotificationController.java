package com.genealogy.controller;

import com.genealogy.entity.UserNotification;
import com.genealogy.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService notificationService;

    /**
     * 获取未读通知
     */
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnread(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        List<UserNotification> notifications = notificationService.getUnread(userId);
        long unreadCount = notificationService.countUnread(userId);
        result.put("success", true);
        result.put("data", notifications);
        result.put("unreadCount", unreadCount);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有通知
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAll(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        List<UserNotification> notifications = notificationService.getAll(userId);
        result.put("success", true);
        result.put("data", notifications);
        result.put("total", notifications.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 标记已读
     */
    @PostMapping("/read/{id}")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAsRead(id, userId);
        result.put("success", true);
        result.put("message", "标记已读成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 标记全部已读
     */
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAllAsRead(userId);
        result.put("success", true);
        result.put("message", "标记全部已读成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        long count = notificationService.countUnread(userId);
        result.put("success", true);
        result.put("count", count);
        return ResponseEntity.ok(result);
    }
}
