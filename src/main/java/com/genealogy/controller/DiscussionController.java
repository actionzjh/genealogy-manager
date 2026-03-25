package com.genealogy.controller;

import com.genealogy.entity.Discussion;
import com.genealogy.repository.DiscussionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 寻亲公告/讨论控制器
 */
@RestController
@RequestMapping("/api/discussion")
@CrossOrigin(origins = "*")
public class DiscussionController {

    @Autowired
    private DiscussionRepository discussionRepository;

    /**
     * 获取寻亲公告列表（分页）
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "search") String type) {

        Map<String, Object> result = new HashMap<>();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Discussion> pageData = discussionRepository.findByTypeAndStatus(type, "open", pageable);

        result.put("code", 0);
        result.put("data", pageData.getContent());
        result.put("total", pageData.getTotalElements());
        result.put("pages", pageData.getTotalPages());
        result.put("currentPage", page);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Optional<Discussion> discussion = discussionRepository.findById(id);

        if (discussion.isEmpty()) {
            result.put("code", 404);
            result.put("message", "公告不存在");
            return ResponseEntity.notFound().build();
        }

        // 增加浏览次数
        Discussion d = discussion.get();
        d.setViewCount(d.getViewCount() + 1);
        discussionRepository.save(d);

        result.put("code", 0);
        result.put("data", d);
        return ResponseEntity.ok(result);
    }

    /**
     * 用户发布寻亲公告
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Discussion discussion,
            Authentication authentication) {

        Map<String, Object> result = new HashMap<>();

        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        Long userId = (Long) authentication.getPrincipal();
        discussion.setUserId(userId);

        if (discussion.getType() == null) {
            discussion.setType("search"); // 默认寻亲
        }

        Discussion saved = discussionRepository.save(discussion);
        result.put("code", 0);
        result.put("message", "发布成功");
        result.put("data", saved);
        return ResponseEntity.ok(result);
    }

    /**
     * 用户关闭自己的公告（已找到）
     */
    @PutMapping("/{id}/close")
    public ResponseEntity<Map<String, Object>> close(
            @PathVariable Long id,
            Authentication authentication) {

        Map<String, Object> result = new HashMap<>();

        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        Long userId = (Long) authentication.getPrincipal();
        Optional<Discussion> discussionOpt = discussionRepository.findById(id);

        if (discussionOpt.isEmpty()) {
            result.put("code", 404);
            result.put("message", "公告不存在");
            return ResponseEntity.notFound().build();
        }

        Discussion discussion = discussionOpt.get();
        if (!discussion.getUserId().equals(userId)) {
            result.put("code", 403);
            result.put("message", "无权操作此公告");
            return ResponseEntity.status(403).body(result);
        }

        discussion.setStatus("closed");
        discussionRepository.save(discussion);

        result.put("code", 0);
        result.put("message", "已关闭公告");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取最新公告（首页展示）
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatest() {
        Map<String, Object> result = new HashMap<>();
        var discussions = discussionRepository.findTop10ByTypeAndStatusOrderByCreatedAtDesc("search", "open");

        result.put("code", 0);
        result.put("data", discussions);
        return ResponseEntity.ok(result);
    }
}
