package com.genealogy.controller;

import com.genealogy.service.GenealogyCollaboratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家谱协作者权限控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/collaborator")
@RequiredArgsConstructor
public class GenealogyCollaboratorController {

    private final GenealogyCollaboratorService collaboratorService;

    /**
     * 获取家谱协作者列表
     */
    @GetMapping("/list/{genealogyId}")
    public ResponseEntity<Map<String, Object>> list(@PathVariable Long genealogyId) {
        Map<String, Object> result = new HashMap<>();
        List<GenealogyCollaboratorService.CollaboratorInfo> collaborators =
                collaboratorService.getCollaborators(genealogyId);
        result.put("success", true);
        result.put("data", collaborators);
        return ResponseEntity.ok(result);
    }

    /**
     * 邀请协作者
     */
    @PostMapping("/invite")
    public ResponseEntity<Map<String, Object>> invite(
            @RequestParam Long genealogyId,
            @RequestParam Long inviterUserId,
            @RequestParam String inviteeUsername,
            @RequestParam(defaultValue = "EDITOR") String role) {
        Map<String, Object> result = new HashMap<>();
        GenealogyCollaboratorService.Result serviceResult =
                collaboratorService.inviteCollaborator(genealogyId, inviterUserId, inviteeUsername, role);
        result.put("success", serviceResult.isSuccess());
        if (serviceResult.isSuccess()) {
            result.put("data", serviceResult.getData());
            result.put("message", "邀请成功");
        } else {
            result.put("message", serviceResult.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 更新协作者权限
     */
    @PostMapping("/update-role")
    public ResponseEntity<Map<String, Object>> updateRole(
            @RequestParam Long genealogyId,
            @RequestParam Long operatorUserId,
            @RequestParam Long targetUserId,
            @RequestParam String newRole) {
        Map<String, Object> result = new HashMap<>();
        GenealogyCollaboratorService.Result serviceResult =
                collaboratorService.updateRole(genealogyId, operatorUserId, targetUserId, newRole);
        result.put("success", serviceResult.isSuccess());
        if (serviceResult.isSuccess()) {
            result.put("data", serviceResult.getData());
            result.put("message", "更新权限成功");
        } else {
            result.put("message", serviceResult.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 移除协作者 / 自己退出
     */
    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> remove(
            @RequestParam Long genealogyId,
            @RequestParam Long operatorUserId,
            @RequestParam Long targetUserId) {
        Map<String, Object> result = new HashMap<>();
        GenealogyCollaboratorService.Result serviceResult =
                collaboratorService.removeCollaborator(genealogyId, operatorUserId, targetUserId);
        result.put("success", serviceResult.isSuccess());
        if (serviceResult.isSuccess()) {
            result.put("message", "移除成功");
        } else {
            result.put("message", serviceResult.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
