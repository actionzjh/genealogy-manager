package com.genealogy.controller;

import com.genealogy.entity.Genealogy;
import com.genealogy.service.GenealogyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 家谱管理接口
 */
@RestController
@RequestMapping("/api/genealogy")
@CrossOrigin(origins = "*")
public class GenealogyController {

    @Autowired
    private GenealogyService genealogyService;

    /**
     * 新增家谱
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Genealogy genealogy) {
        Map<String, Object> result = new HashMap<>();
        try {
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
     * 更新家谱
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Genealogy genealogy) {
        Map<String, Object> result = new HashMap<>();
        try {
            Optional<Genealogy> existing = genealogyService.findById(id);
            if (existing.isEmpty()) {
                result.put("code", 404);
                result.put("message", "家谱不存在");
                return ResponseEntity.notFound().build();
            }
            genealogy.setId(id);
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
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Optional<Genealogy> genealogy = genealogyService.findById(id);
        if (genealogy.isEmpty()) {
            result.put("code", 404);
            result.put("message", "家谱不存在");
            return ResponseEntity.notFound().build();
        }
        result.put("code", 0);
        result.put("data", genealogy.get());
        return ResponseEntity.ok(result);
    }

    /**
     * 分页查询所有家谱
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new HashMap<>();
        Page<Genealogy> pageData = genealogyService.findAll(page, size);
        result.put("code", 0);
        result.put("data", pageData.getContent());
        result.put("total", pageData.getTotalElements());
        result.put("pages", pageData.getTotalPages());
        result.put("currentPage", page);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有家谱
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAll() {
        Map<String, Object> result = new HashMap<>();
        List<Genealogy> list = genealogyService.findAll();
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 搜索家谱
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        List<Genealogy> list = genealogyService.search(keyword);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 根据姓氏查找
     */
    @GetMapping("/surname/{surname}")
    public ResponseEntity<Map<String, Object>> findBySurname(@PathVariable String surname) {
        Map<String, Object> result = new HashMap<>();
        List<Genealogy> list = genealogyService.findBySurname(surname);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 删除家谱
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
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
     * 获取统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("totalGenealogies", genealogyService.count());
        return ResponseEntity.ok(result);
    }
}
