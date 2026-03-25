package com.genealogy.controller;

import com.genealogy.entity.Branch;
import com.genealogy.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/branch")
@CrossOrigin(origins = "*")
public class BranchController {

    @Autowired
    private BranchService branchService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Branch branch) {
        Map<String, Object> result = new HashMap<>();
        try {
            Branch saved = branchService.save(branch);
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

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Branch branch) {
        Map<String, Object> result = new HashMap<>();
        Optional<Branch> existing = branchService.findById(id);
        if (existing.isEmpty()) {
            result.put("code", 404);
            result.put("message", "支系不存在");
            return ResponseEntity.status(404).body(result);
        }
        try {
            branch.setId(id);
            Branch saved = branchService.save(branch);
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

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Optional<Branch> branch = branchService.findById(id);
        if (branch.isEmpty()) {
            result.put("code", 404);
            result.put("message", "支系不存在");
            return ResponseEntity.status(404).body(result);
        }
        result.put("code", 0);
        result.put("data", branch.get());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAll() {
        Map<String, Object> result = new HashMap<>();
        List<Branch> list = branchService.findAll();
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/genealogy/{genealogyId}")
    public ResponseEntity<Map<String, Object>> findByGenealogy(@PathVariable Long genealogyId) {
        Map<String, Object> result = new HashMap<>();
        List<Branch> list = branchService.findByGenealogyId(genealogyId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<Map<String, Object>> findChildren(@PathVariable Long parentId) {
        Map<String, Object> result = new HashMap<>();
        List<Branch> list = branchService.findChildren(parentId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        List<Branch> list = branchService.search(keyword);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            branchService.deleteById(id);
            result.put("code", 0);
            result.put("message", "删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("totalBranches", branchService.count());
        return ResponseEntity.ok(result);
    }
}
