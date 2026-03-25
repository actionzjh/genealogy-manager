package com.genealogy.controller;

import com.genealogy.entity.Surname;
import com.genealogy.repository.SurnameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 姓氏百科控制器
 */
@RestController
@RequestMapping("/api/surname")
@CrossOrigin(origins = "*")
public class SurnameController {

    @Autowired
    private SurnameRepository surnameRepository;

    /**
     * 搜索姓氏
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        List<Surname> list = surnameRepository.findByNameContainingIgnoreCase(keyword);

        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取姓氏详情
     */
    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable String name) {
        Map<String, Object> result = new HashMap<>();
        Optional<Surname> surname = surnameRepository.findByNameIgnoreCase(name);

        if (surname.isEmpty()) {
            result.put("code", 404);
            result.put("message", "未找到该姓氏百科");
            return ResponseEntity.notFound().build();
        }

        result.put("code", 0);
        result.put("data", surname.get());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取热门姓氏（按家谱数量排序）
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopular() {
        Map<String, Object> result = new HashMap<>();
        List<Surname> list = surnameRepository.findAllByOrderByGenealogyCountDesc();

        result.put("code", 0);
        result.put("data", list);
        return ResponseEntity.ok(result);
    }

    /**
     * 创建/更新姓氏百科（管理员功能，开放给用户贡献）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody Surname surname) {
        Map<String, Object> result = new HashMap<>();

        Surname saved = surnameRepository.save(surname);
        result.put("code", 0);
        result.put("message", "保存成功");
        result.put("data", saved);
        return ResponseEntity.ok(result);
    }
}
