package com.genealogy.controller;

import com.genealogy.entity.FamilyMessage;
import com.genealogy.service.FamilyMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宗亲留言板控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/family-message")
@RequiredArgsConstructor
public class FamilyMessageController {

    private final FamilyMessageService messageService;

    /**
     * 发布留言（公开接口，任何人都可以留言）
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody FamilyMessage message,
            HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String clientIp = getClientIp(request);
        FamilyMessageService.Result createResult = messageService.create(message, clientIp);
        result.put("success", createResult.isSuccess());
        result.put("message", createResult.getMessage());
        result.put("data", createResult.getData());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取公开家谱留言列表（分页）
     */
    @GetMapping("/list/{genealogyId}")
    public ResponseEntity<Map<String, Object>> list(
            @PathVariable Long genealogyId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = new HashMap<>();
        Page<FamilyMessage> data = messageService.listApproved(genealogyId, page, size);
        long total = messageService.countApproved(genealogyId);
        result.put("success", true);
        result.put("data", data.getContent());
        result.put("total", total);
        result.put("totalPages", data.getTotalPages());
        result.put("currentPage", data.getNumber() + 1);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取留言回复
     */
    @GetMapping("/replies/{parentId}")
    public ResponseEntity<Map<String, Object>> getReplies(@PathVariable Long parentId) {
        Map<String, Object> result = new HashMap<>();
        List<FamilyMessage> replies = messageService.listReplies(parentId);
        result.put("success", true);
        result.put("data", replies);
        return ResponseEntity.ok(result);
    }

    /**
     * 审核留言 (管理员权限)
     */
    @PostMapping("/approve/{id}")
    public ResponseEntity<Map<String, Object>> approve(
            @PathVariable Long id,
            @RequestParam boolean approved) {
        Map<String, Object> result = new HashMap<>();
        FamilyMessageService.Result approveResult = messageService.approve(id, approved);
        result.put("success", approveResult.isSuccess());
        result.put("message", approveResult.getMessage());
        return ResponseEntity.ok(result);
    }

    /**
     * 删除留言 (管理员权限)
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        FamilyMessageService.Result deleteResult = messageService.delete(id);
        result.put("success", deleteResult.isSuccess());
        result.put("message", deleteResult.getMessage());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For可能包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
