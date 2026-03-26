package com.genealogy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genealogy.config.AiConfig;
import com.genealogy.entity.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 自动生成人物简介服务
 * 根据基本信息生成通顺的传记文字
 */
@Service
public class AiBiographyService {

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 生成人物简介
     * @param person 人物基本信息
     * @param style 风格：wenyan(文言文)/baihua(白话文)
     * @return 生成的简介
     */
    public String generateBiography(Person person, String style) throws Exception {
        String prompt = buildPrompt(person, style);
        return callOpenAiChat(prompt);
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(Person person, String style) {
        StringBuilder info = new StringBuilder();
        info.append("姓名：").append(person.getName()).append("\n");
        if (person.getStyleName() != null && !person.getStyleName().isEmpty()) {
            info.append("字：").append(person.getStyleName()).append("\n");
        }
        if (person.getHao() != null && !person.getHao().isEmpty()) {
            info.append("号：").append(person.getHao()).append("\n");
        }
        if (person.getGeneration() != null) {
            info.append("世代：第").append(person.getGeneration()).append("世\n");
        }
        if (person.getBirthYear() != null && !person.getBirthYear().isEmpty()) {
            info.append("出生：").append(person.getBirthYear()).append("年\n");
        }
        if (person.getDeathYear() != null && !person.getDeathYear().isEmpty()) {
            info.append("逝世：").append(person.getDeathYear()).append("年\n");
        }
        if (person.getTitle() != null && !person.getTitle().isEmpty()) {
            info.append("头衔/官职：").append(person.getTitle()).append("\n");
        }
        if (person.getBirthPlace() != null && !person.getBirthPlace().isEmpty()) {
            info.append("出生地：").append(person.getBirthPlace()).append("\n");
        }
        if (person.getBurialPlace() != null && !person.getBurialPlace().isEmpty()) {
            info.append("葬地：").append(person.getBurialPlace()).append("\n");
        }
        if (person.getNote() != null && !person.getNote().isEmpty()) {
            info.append("现有备注：").append(person.getNote()).append("\n");
        }

        String styleDesc = style.equals("wenyan") ?
                "请用文言文简洁传记风格撰写，符合传统家谱写法" :
                "请用白话文通顺流畅撰写";

        return String.format(
                "你是家谱传记撰写专家，请根据以下人物基本信息，为这个家族人物写一篇%n字左右的简短传记。%s。\n\n" +
                "人物信息：\n%s\n\n" +
                "请直接返回传记内容，不需要多余说明。",
                150, styleDesc, info.toString()
        );
    }

    /**
     * 调用 OpenAI 兼容接口
     */
    private String callOpenAiChat(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiConfig.getOpenaiApiKey());

        Map<String, Object> messages = new HashMap<>();
        ArrayList<Map<String, String>> msgList = new ArrayList<>();
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        msgList.add(userMsg);

        Map<String, Object> body = new HashMap<>();
        body.put("model", aiConfig.getOpenaiModel());
        body.put("messages", msgList);
        body.put("temperature", 0.7);
        body.put("max_tokens", 300);

        HttpEntity<String> request;
        try {
            request = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
        } catch (Exception e) {
            throw e;
        }

        String url = aiConfig.getOpenaiBaseUrl() + "/chat/completions";
        var response = restTemplate.postForEntity(url, request, JsonNode.class);
        JsonNode responseBody = response.getBody();

        if (responseBody == null || !responseBody.has("choices")) {
            throw new Exception("AI API 返回格式错误");
        }

        JsonNode choices = responseBody.get("choices");
        if (choices.isEmpty()) {
            throw new Exception("AI API 返回空结果");
        }

        JsonNode messageNode = choices.get(0).get("message");
        String content = messageNode.get("content").asText();

        return content.trim();
    }
}
