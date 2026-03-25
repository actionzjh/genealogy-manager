package com.genealogy.controller;

import com.genealogy.entity.Person;
import com.genealogy.service.AdvancedSearchService;
import com.genealogy.service.RelationshipCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 高级搜索与血缘关系计算接口
 */
@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private AdvancedSearchService advancedSearchService;

    @Autowired
    private RelationshipCalculator relationshipCalculator;

    /**
     * 高级多条件搜索
     */
    @PostMapping("/advanced")
    public ResponseEntity<Map<String, Object>> advancedSearch(
            @RequestBody AdvancedSearchService.SearchFilter filter,
            Authentication authentication) {

        Map<String, Object> result = new HashMap<>();

        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();

        List<Person> persons = advancedSearchService.advancedSearch(filter, userId);

        result.put("code", 0);
        result.put("data", persons);
        result.put("total", persons.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 关键字搜索
     */
    @GetMapping("/keyword")
    public ResponseEntity<Map<String, Object>> keywordSearch(
            @RequestParam String keyword,
            Authentication authentication) {

        Map<String, Object> result = new HashMap<>();

        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();

        List<Person> persons = advancedSearchService.keywordSearch(keyword, userId);

        result.put("code", 0);
        result.put("data", persons);
        result.put("total", persons.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 查找共同祖先
     */
    @GetMapping("/common-ancestors")
    public ResponseEntity<Map<String, Object>> getCommonAncestors(
            @RequestParam Long personId1,
            @RequestLong personId2,
            Authentication authentication) {

        Map<String, Object> result = new HashMap<>();

        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        List<Person> ancestors = relationshipCalculator.findCommonAncestors(personId1, personId2);

        result.put("code", 0);
        result.put("commonAncestors", ancestors);
        result.put("count", ancestors.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 计算亲缘系数
     */
    @GetMapping("/coefficient")
    public ResponseEntity<Map<String, Object>> getRelationshipCoefficient(
            @RequestParam Long personId1,
            @RequestParam Long personId2,
            Authentication authentication) {

        Map<String, Object> result = new HashMap<>();

        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        double coefficient = relationshipCalculator.calculateCoefficientOfRelationship(personId1, personId2);
        Optional<Person> mrca = relationshipCalculator.findMostRecentCommonAncestor(personId1, personId2);

        result.put("code", 0);
        result.put("coefficient", coefficient);
        result.put("coefficientPercent", String.format("%.4f%%", coefficient * 100));
        if (mrca.isPresent()) {
            result.put("mostRecentCommonAncestor", mrca.get());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 查找某人的所有后代
     */
    @GetMapping("/descendants/{ancestorId}")
    public ResponseEntity<Map<String, Object>> getDescendants(
            @PathVariable Long ancestorId,
            Authentication authentication) {

        Map<String, Object> result = new HashMap<>();

        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        List<Person> descendants = relationshipCalculator.findAllDescendants(ancestorId);

        result.put("code", 0);
        result.put("data", descendants);
        result.put("total", descendants.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取家谱统计数据
     */
    @GetMapping("/stats/{genealogyId}")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @PathVariable Long genealogyId,
            Authentication authentication) {

        Map<String, Object> result = new HashMap<>();

        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        Map<String, Long> genderStats = relationshipCalculator.countByGender(genealogyId);
        Map<Integer, Long> generationStats = relationshipCalculator.countByGeneration(genealogyId);
        Double avgInterval = relationshipCalculator.calculateAverageGenerationInterval(genealogyId);

        result.put("code", 0);
        result.put("gender", genderStats);
        result.put("generation", generationStats);
        result.put("averageGenerationInterval", avgInterval);
        return ResponseEntity.ok(result);
    }
}
