package com.genealogy.controller;

import com.genealogy.entity.Genealogy;
import com.genealogy.service.GenealogyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 家谱管理接口
 */
@RestController@RequestMapping("/api/genealogy")
@CrossOrigin(origins = "*")
public class GenealogyController {

    @Autowired
    private GenealogyService genealogyService;

    /**
     * 新增家谱（需要登录）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Genealogy genealogy, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (authentication == null || authentication.getPrincipal() == null) {
                result.put("code", 401);
                result.put("message", "请先登录");
                return ResponseEntity.status(401).body(result);
            }
            Long userId = (Long) authentication.getPrincipal();
            genealogy.setUserId(userId);
            Genealogy saved = genealogyService.save(genealogy);
            result.put("code", 0);
            result.put("message", "创建成功");
            result.put("data", saved);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 更新家谱（需要验证所有权）
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Genealogy genealogy, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (authentication == null || authentication.getPrincipal() == null) {
                result.put("code", 401);
                result.put("message", "请先登录");
                return ResponseEntity.status(401).body(result);
            }
            Long userId = (Long) authentication.getPrincipal();
            if (!genealogyService.isOwner(id, userId)) {
                result.put("code", 403);
                result.put("message", "无权操作此家谱");
                return ResponseEntity.status(403).body(result);
            }
            Optional<Genealogy> existing = genealogyService.findById(id);
            if (existing.isEmpty()) {
                result.put("code", 404);
                result.put("message", "家谱不存在");
                return ResponseEntity.notFound().build();
            }
            genealogy.setId(id);
            genealogy.setUserId(userId);
            Genealogy saved = genealogyService.save(genealogy);
            result.put("code", 0);
            result.put("message", "更新成功");
            result.put("data", saved);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 根据ID获取家谱
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        Optional<Genealogy> genealogy = genealogyService.findById(id);
        if (genealogy.isEmpty()) {
            result.put("code", 404);
            result.put("message", "家谱不存在");
            return ResponseEntity.notFound().build();
        }
        if (!genealogy.get().getUserId().equals(userId)) {
            result.put("code", 403);
            result.put("message", "无权访问此家谱");
            return ResponseEntity.status(403).body(result);
        }
        result.put("code", 0);
        result.put("data", genealogy.get());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取当前用户的所有家谱
     */
    @GetMapping
    public ResponseEntity<List<Genealogy>> getMyGenealogies(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = (Long) authentication.getPrincipal();
        List<Genealogy> list = genealogyService.findByUserId(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * 分页查询当前用户的家谱
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        Page<Genealogy> pageData = genealogyService.findByUserId(userId, page, size);
        result.put("code", 0);
        result.put("data", pageData.getContent());
        result.put("total", pageData.getTotalElements());
        result.put("pages", pageData.getTotalPages());
        result.put("currentPage", page);
        return ResponseEntity.ok(result);
    }

    /**
     * 搜索当前用户的家谱
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String keyword, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        List<Genealogy> list = genealogyService.search(userId, keyword);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 删除家谱（需要验证所有权）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (authentication == null || authentication.getPrincipal() == null) {
                result.put("code", 401);
                result.put("message", "请先登录");
                return ResponseEntity.status(401).body(result);
            }
            Long userId = (Long) authentication.getPrincipal();
            if (!genealogyService.isOwner(id, userId)) {
                result.put("code", 403);
                result.put("message", "无权删除此家谱");
                return ResponseEntity.status(403).body(result);
            }
            genealogyService.deleteById(id);
            result.put("code", 0);
            result.put("message", "删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取当前用户统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        result.put("code", 0);
        result.put("totalGenealogies", genealogyService.countByUserId(userId));
        return ResponseEntity.ok(result);
    }
}
