package com.genealogy.controller;

import com.genealogy.entity.PersonRelationGraph;
import com.genealogy.entity.User;
import com.genealogy.service.PersonRelationGraphService;
import com.genealogy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 血缘关系可视化控制器
 */
@RestController
@RequestMapping("/api/relation-graph")
public class PersonRelationGraphController {

    @Autowired
    private PersonRelationGraphService relationGraphService;

    @Autowired
    private UserService userService;

    /**
     * 创建/保存关系视图
     */
    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody PersonRelationGraph graph, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getCurrentUser(principal);
        if (user == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        graph.setUserId(user.getId());
        PersonRelationGraph saved = relationGraphService.save(graph);
        result.put("success", true);
        result.put("data", saved);
        return result;
    }

    /**
     * 更新关系视图
     */
    @PostMapping("/update/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody PersonRelationGraph graph, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getCurrentUser(principal);
        if (user == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        if (!relationGraphService.findById(id).isPresent()) {
            result.put("success", false);
            result.put("message", "视图不存在");
            return result;
        }
        PersonRelationGraph existing = relationGraphService.findById(id).get();
        if (!existing.getUserId().equals(user.getId())) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }
        graph.setId(id);
        graph.setUserId(user.getId());
        graph.setGenealogyId(existing.getGenealogyId());
        PersonRelationGraph saved = relationGraphService.save(graph);
        result.put("success", true);
        result.put("data", saved);
        return result;
    }

    /**
     * 删除视图
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getCurrentUser(principal);
        if (user == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        if (!relationGraphService.findById(id).isPresent()) {
            result.put("success", false);
            result.put("message", "视图不存在");
            return result;
        }
        PersonRelationGraph existing = relationGraphService.findById(id).get();
        if (!existing.getUserId().equals(user.getId())) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }
        relationGraphService.delete(id);
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    /**
     * 列出用户在该家谱下的所有视图
     */
    @GetMapping("/list/{genealogyId}")
    public Map<String, Object> list(@PathVariable Long genealogyId, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getCurrentUser(principal);
        if (user == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        List<PersonRelationGraph> list = relationGraphService.listByUserAndGenealogy(genealogyId, user.getId());
        result.put("success", true);
        result.put("data", list);
        return result;
    }

    /**
     * 生成实时关系图数据
     */
    @GetMapping("/build")
    public Map<String, Object> build(@RequestParam Long centerPersonId, @RequestParam(defaultValue = "3") Integer depth, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getCurrentUser(principal);
        if (user == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        Map<String, Object> graphData = relationGraphService.buildRelationGraph(centerPersonId, depth);
        result.put("success", true);
        result.put("data", graphData);
        return result;
    }

    /**
     * 获取公开图数据（公共页面）
     */
    @GetMapping("/public/build")
    public Map<String, Object> buildPublic(@RequestParam Long centerPersonId, @RequestParam(defaultValue = "3") Integer depth) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> graphData = relationGraphService.buildRelationGraph(centerPersonId, depth);
        result.put("success", true);
        result.put("data", graphData);
        return result;
    }
}
