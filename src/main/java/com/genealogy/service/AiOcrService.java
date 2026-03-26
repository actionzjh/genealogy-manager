package com.genealogy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genealogy.config.AiConfig;
import com.genealogy.entity.AiOcrResult;
import com.genealogy.repository.AiOcrResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI OCR 结构化识别服务
 * 将OCR识别出的原始文本通过大模型提取结构化人物数据
 */
@Service
public class AiOcrService {

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private AiOcrResultRepository aiOcrResultRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 创建识别任务
     */
    public AiOcrResult createTask(Long userId, Long genealogyId, String originalText) {
        AiOcrResult task = new AiOcrResult();
        task.setUserId(userId);
        task.setGenealogyId(genealogyId);
        task.setOriginalText(originalText);
        task.setStatus("processing");
        return aiOcrResultRepository.save(task);
    }

    /**
     * 执行结构化识别
     */
    public AiOcrResult processStructuredExtraction(Long taskId) throws Exception {
        AiOcrResult task = aiOcrResultRepository.findById(taskId).orElse(null);
        if (task == null) {
            return null;
        }

        try {
            String prompt = buildExtractionPrompt(task.getOriginalText());
            String response = callOpenAiChat(prompt);
            // 保存结构化结果
            task.setStructuredJson(response);
            task.setStatus("done");
            aiOcrResultRepository.save(task);
            return task;
        } catch (Exception e) {
            task.setStatus("failed");
            task.setErrorMessage(e.getMessage());
            aiOcrResultRepository.save(task);
            throw e;
        }
    }

    /**
     * 直接同步提取结构化数据
     */
    public Map<String, Object> extractStructured(String originalText) throws Exception {
        String prompt = buildExtractionPrompt(originalText);
        String response = callOpenAiChat(prompt);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, Map.class);
    }

    /**
     * 构建提示词
     */
    private String buildExtractionPrompt(String ocrText) {
        return "你是家谱OCR结构化提取专家。请从下面这段家谱OCR识别文本中，提取出每个人物的结构化信息。\n\n" +
                "请严格按照JSON格式返回，格式为：\n" +
                "{\n" +
                "  \"people\": [\n" +
                "    {\n" +
                "      \"name\": \"姓名\",\n" +
                "      \"styleName\": \"字(没有则留空)\",\n" +
                "      \"hao\": \"号(没有则留空)\",\n" +
                "      \"generation\": 世代数字(如第18世填18，没有则留null),\n" +
                "      \"birthYear\": \"出生年份(没有则留空)\",\n" +
                "      \"deathYear\": \"逝世年份(没有则留空)\",\n" +
                "      \"gender\": \"male/female(根据关系推断，不确定留null)\",\n" +
                "      \"fatherName\": \"父亲姓名(没有则留空)\",\n" +
                "      \"motherName\": \"母亲姓名(没有则留空)\",\n" +
                "      \"spouseName\": \"配偶姓名(没有则留空)\",\n" +
                "      \"childrenNames\": [子女姓名数组，没有则留空数组],\n" +
                "      \"birthPlace\": \"出生地/祖籍(没有则留空)\",\n" +
                "      \"burialPlace\": \"葬地(没有则留空)\",\n" +
                "      \"biography\": \"生平简介(提取原文)\",\n" +
                "      \"title\": \"官职/功名/头衔(没有则留空)\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "OCR文本内容：\n" + ocrText + "\n\n" +
                "请只返回合法JSON，不要加任何其他说明文字。";
    }

    /**
     * 调用 OpenAI 兼容接口
     */
    private String callOpenAiChat(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiConfig.getOpenaiApiKey());

        Map<String, Object> messages = new HashMap<>();
        List<Map<String, String>> msgList = new ArrayList<>();
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        msgList.add(userMsg);

        Map<String, Object> body = new HashMap<>();
        body.put("model", aiConfig.getOpenaiModel());
        body.put("messages", msgList);
        body.put("temperature", 0.1);

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

        JsonNode message = choices.get(0).get("message");
        String content = message.get("content").asText();

        // 清理内容，如果被markdown包裹
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring(7);
        }
        if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        return content.trim();
    }

    /**
     * 获取用户家谱下的所有识别结果
     */
    public List<AiOcrResult> listByUserAndGenealogy(Long userId, Long genealogyId) {
        return aiOcrResultRepository.findByUserIdAndGenealogyIdOrderByCreatedAtDesc(userId, genealogyId);
    }

    /**
     * 获取任务详情
     */
    public AiOcrResult getTask(Long id) {
        return aiOcrResultRepository.findById(id).orElse(null);
    }

    /**
     * 删除任务
     */
    public void delete(Long id) {
        aiOcrResultRepository.deleteById(id);
    }
}
