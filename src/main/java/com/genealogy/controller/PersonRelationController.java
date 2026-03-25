package com.genealogy.controller;

import com.genealogy.entity.PersonRelation;
import com.genealogy.service.PersonRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/person-relation")
@CrossOrigin(origins = "*")
public class PersonRelationController {

    @Autowired
    private PersonRelationService personRelationService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody PersonRelation relation) {
        Map<String, Object> result = new HashMap<>();
        try {
            PersonRelation saved = personRelationService.save(relation);
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
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody PersonRelation relation) {
        Map<String, Object> result = new HashMap<>();
        Optional<PersonRelation> existing = personRelationService.findById(id);
        if (existing.isEmpty()) {
            result.put("code", 404);
            result.put("message", "人物关系不存在");
            return ResponseEntity.status(404).body(result);
        }
        try {
            relation.setId(id);
            PersonRelation saved = personRelationService.save(relation);
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
        Optional<PersonRelation> relation = personRelationService.findById(id);
        if (relation.isEmpty()) {
            result.put("code", 404);
            result.put("message", "人物关系不存在");
            return ResponseEntity.status(404).body(result);
        }
        result.put("code", 0);
        result.put("data", relation.get());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAll() {
        Map<String, Object> result = new HashMap<>();
        List<PersonRelation> list = personRelationService.findAll();
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/genealogy/{genealogyId}")
    public ResponseEntity<Map<String, Object>> findByGenealogy(@PathVariable Long genealogyId) {
        Map<String, Object> result = new HashMap<>();
        List<PersonRelation> list = personRelationService.findByGenealogyId(genealogyId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<Map<String, Object>> findByPerson(@PathVariable Long personId) {
        Map<String, Object> result = new HashMap<>();
        List<PersonRelation> list = personRelationService.findByPersonId(personId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/person/{personId}/parents")
    public ResponseEntity<Map<String, Object>> findParents(@PathVariable Long personId) {
        Map<String, Object> result = new HashMap<>();
        List<PersonRelation> list = personRelationService.findParents(personId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/person/{personId}/spouses")
    public ResponseEntity<Map<String, Object>> findSpouses(@PathVariable Long personId) {
        Map<String, Object> result = new HashMap<>();
        List<PersonRelation> list = personRelationService.findSpouses(personId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/person/{personId}/children")
    public ResponseEntity<Map<String, Object>> findChildren(@PathVariable Long personId) {
        Map<String, Object> result = new HashMap<>();
        List<PersonRelation> list = personRelationService.findChildren(personId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            personRelationService.deleteById(id);
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
        result.put("totalRelations", personRelationService.count());
        return ResponseEntity.ok(result);
    }
}
