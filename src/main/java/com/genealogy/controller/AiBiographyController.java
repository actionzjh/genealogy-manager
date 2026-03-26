package com.genealogy.controller;

import com.genealogy.entity.Person;
import com.genealogy.entity.User;
import com.genealogy.repository.PersonRepository;
import com.genealogy.service.AiBiographyService;
import com.genealogy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AI 自动生成人物简介控制器
 */
@RestController
@RequestMapping("/api/ai-biography")
public class AiBiographyController {

    @Autowired
    private AiBiographyService aiBiographyService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private UserService userService;

    /**
     * 生成人物简介
     */
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody Map<String, Object> request, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getCurrentUser(principal);
        if (user == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        Long personId = Long.valueOf(request.get("personId").toString());
        String style = request.containsKey("style") ? request.get("style").toString() : "baihua";

        Optional<Person> personOpt = personRepository.findById(personId);
        if (!personOpt.isPresent()) {
            result.put("success", false);
            result.put("message", "人物不存在");
            return result;
        }

        try {
            String biography = aiBiographyService.generateBiography(personOpt.get(), style);
            result.put("success", true);
            result.put("data", biography);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "生成失败：" + e.getMessage());
        }
        return result;
    }
}
