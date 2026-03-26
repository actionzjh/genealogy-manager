package com.genealogy.controller;

import com.genealogy.entity.AiOcrResult;
import com.genealogy.entity.User;
import com.genealogy.service.AiOcrService;
import com.genealogy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI OCR 结构化识别控制器
 */
@RestController
@RequestMapping("/api/ai-ocr")
public class AiOcrController {

    @Autowired
    private AiOcrService aiOcrService;

    @Autowired
    private UserService userService;

    /**
     * 提交OCR文本进行结构化提取
     */
    @PostMapping("/extract")
    public Map<String, Object> extract(@RequestBody Map<String, Object> request, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getCurrentUser(principal);
        if (user == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        Long genealogyId = Long.valueOf(request.get("genealogyId").toString());
        String originalText = request.get("originalText").toString();

        try {
            // 同步直接提取
            Map<String, Object> structured = aiOcrService.extractStructured(originalText);
            result.put("success", true);
            result.put("data", structured);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "AI提取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 创建异步提取任务
     */
    @PostMapping("/create-task")
    public Map<String, Object> createTask(@RequestBody Map<String, Object> request, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getCurrentUser(principal);
        if (user == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        Long genealogyId = Long.valueOf(request.get("genealogyId").toString());
        String originalText = request.get("originalText").toString();

        AiOcrResult task = aiOcrService.createTask(user.getId(), genealogyId, originalText);

        try {
            aiOcrService.processStructuredExtraction(task.getId());
            result.put("success", true);
            result.put("data", task);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "处理失败：" + e.getMessage());
            result.put("data", task);
        }
        return result;
    }

    /**
     * 获取任务状态
     */
    @GetMapping("/task/{id}")
    public Map<String, Object> getTask(@PathVariable Long id, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getCurrentUser(principal);
        if (user == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        AiOcrResult task = aiOcrService.getTask(id);
        if (task == null || !task.getUserId().equals(user.getId())) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }

        result.put("success", true);
        result.put("data", task);
        return result;
    }

    /**
     * 列出用户所有识别任务
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

        List<AiOcrResult> tasks = aiOcrService.listByUserAndGenealogy(user.getId(), genealogyId);
        result.put("success", true);
        result.put("data", tasks);
        return result;
    }

    /**
     * 删除任务
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

        AiOcrResult task = aiOcrService.getTask(id);
        if (task == null || !task.getUserId().equals(user.getId())) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }

        aiOcrService.delete(id);
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }
}
