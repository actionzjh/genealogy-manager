package com.genealogy.controller;

import com.genealogy.entity.Family;
import com.genealogy.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 家庭关系管理接口
 */
@RestController
@RequestMapping("/api/family")
@CrossOrigin(origins = "*")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    /**
     * 新增家庭关系
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Family family) {
        Map<String, Object> result = new HashMap<>();
        try {
            Family saved = familyService.save(family);
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
     * 更新家庭关系
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Family family) {
        Map<String, Object> result = new HashMap<>();
        try {
            Optional<Family> existing = familyService.findById(id);
            if (existing.isEmpty()) {
                result.put("code", 404);
                result.put("message", "家庭关系不存在");
                return ResponseEntity.notFound().build();
            }
            family.setId(id);
            Family saved = familyService.save(family);
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
     * 根据ID获取家庭关系
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Optional<Family> family = familyService.findById(id);
        if (family.isEmpty()) {
            result.put("code", 404);
            result.put("message", "家庭关系不存在");
            return ResponseEntity.notFound().build();
        }
        result.put("code", 0);
        result.put("data", family.get());
        return ResponseEntity.ok(result);
    }

    /**
     * 根据家谱ID获取所有家庭
     */
    @GetMapping("/genealogy/{genealogyId}")
    public ResponseEntity<Map<String, Object>> findByGenealogy(@PathVariable Long genealogyId) {
        Map<String, Object> result = new HashMap<>();
        List<Family> list = familyService.findByGenealogyId(genealogyId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 根据丈夫ID查找
     */
    @GetMapping("/husband/{husbandId}")
    public ResponseEntity<Map<String, Object>> findByHusband(@PathVariable Long husbandId) {
        Map<String, Object> result = new HashMap<>();
        List<Family> list = familyService.findByHusbandId(husbandId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 根据妻子ID查找
     */
    @GetMapping("/wife/{wifeId}")
    public ResponseEntity<Map<String, Object>> findByWife(@PathVariable Long wifeId) {
        Map<String, Object> result = new HashMap<>();
        List<Family> list = familyService.findByWifeId(wifeId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 删除家庭关系
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            familyService.deleteById(id);
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
        result.put("totalFamilies", familyService.count());
        return ResponseEntity.ok(result);
    }
}
