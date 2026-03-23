package com.genealogy.controller;

import com.genealogy.entity.Person;
import com.genealogy.service.PersonService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 人物管理接口
 */
@RestController
@RequestMapping("/api/person")
@CrossOrigin(origins = "*")
public class PersonController {

    @Autowired
    private PersonService personService;

    private final Gson gson = new Gson();

    /**
     * 新增人物
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Person person) {
        Map<String, Object> result = new HashMap<>();
        try {
            Person saved = personService.save(person);
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
     * 更新人物
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Person person) {
        Map<String, Object> result = new HashMap<>();
        try {
            Optional<Person> existing = personService.findById(id);
            if (existing.isEmpty()) {
                result.put("code", 404);
                result.put("message", "人物不存在");
                return ResponseEntity.notFound().build();
            }
            person.setId(id);
            Person saved = personService.save(person);
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
     * 根据ID获取人物
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Optional<Person> person = personService.findById(id);
        if (person.isEmpty()) {
            result.put("code", 404);
            result.put("message", "人物不存在");
            return ResponseEntity.notFound().build();
        }
        result.put("code", 0);
        result.put("data", person.get());
        return ResponseEntity.ok(result);
    }

    /**
     * 分页查询所有人物
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = new HashMap<>();
        Page<Person> pageData = personService.findAll(page, size);
        result.put("code", 0);
        result.put("data", pageData.getContent());
        result.put("total", pageData.getTotalElements());
        result.put("pages", pageData.getTotalPages());
        result.put("currentPage", page);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有人物（不分页）
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAll() {
        Map<String, Object> result = new HashMap<>();
        List<Person> list = personService.findAll();
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 搜索人物
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        List<Person> list = personService.search(keyword);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 根据支系查找
     */
    @GetMapping("/branch/{branch}")
    public ResponseEntity<Map<String, Object>> findByBranch(@PathVariable String branch) {
        Map<String, Object> result = new HashMap<>();
        List<Person> list = personService.findByBranch(branch);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 根据父亲ID查找子女
     */
    @GetMapping("/children/father/{fatherId}")
    public ResponseEntity<Map<String, Object>> findChildrenByFather(@PathVariable Long fatherId) {
        Map<String, Object> result = new HashMap<>();
        List<Person> list = personService.findChildrenByFatherId(fatherId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 删除人物
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            personService.deleteById(id);
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
        result.put("totalPeople", personService.count());
        result.put("maxGeneration", personService.getMaxGeneration());
        result.put("maleCount", personService.countByGender("M"));
        result.put("femaleCount", personService.countByGender("F"));
        return ResponseEntity.ok(result);
    }
}
