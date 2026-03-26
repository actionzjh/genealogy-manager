package com.genealogy.controller;

import com.genealogy.entity.PersonVersion;
import com.genealogy.service.PersonVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 人物版本控制器 - 版本回滚
 */
@Slf4j
@RestController
@RequestMapping("/api/person-version")
@RequiredArgsConstructor
public class PersonVersionController {

    private final PersonVersionService versionService;

    /**
     * 获取版本列表
     */
    @GetMapping("/list/{personId}")
    public ResponseEntity<Map<String, Object>> list(
            @PathVariable Long personId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new HashMap<>();
        Page<PersonVersion> data = versionService.listVersions(personId, page, size);
        result.put("success", true);
        result.put("data", data.getContent());
        result.put("total", data.getTotalElements());
        result.put("totalPages", data.getTotalPages());
        result.put("currentPage", data.getNumber() + 1);
        return ResponseEntity.ok(result);
    }

    /**
     * 回滚到指定版本
     */
    @PostMapping("/rollback/{versionId}")
    public ResponseEntity<Map<String, Object>> rollback(
            @PathVariable Long versionId,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        PersonVersionService.Result serviceResult = versionService.rollback(versionId, userId);
        result.put("success", serviceResult.isSuccess());
        result.put("message", serviceResult.isSuccess() ? "回滚成功" : serviceResult.getMessage());
        if (serviceResult.isSuccess()) {
            result.put("data", serviceResult.getData());
        }
        return ResponseEntity.ok(result);
    }
}
