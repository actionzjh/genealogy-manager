package com.genealogy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

/**
 * OCR老谱识别服务 - 对接百度文字识别API
 * 使用前需要配置 application.properties:
 * ocr.baidu.api-key=your-api-key
 * ocr.baidu.secret-key=your-secret-key
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrOldGenealogyService {

    @Value("${ocr.baidu.api-key:}")
    private String apiKey;

    @Value("${ocr.baidu.secret-key:}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * OCR识别结果
     */
    @lombok.Data
    public static class OcrResult {
        private boolean success;
        private String message;
        private List<OcrWord> words;

        public static OcrResult success(List<OcrWord> words) {
            OcrResult result = new OcrResult();
            result.setSuccess(true);
            result.setMessage("识别成功");
            result.setWords(words);
            return result;
        }

        public static OcrResult error(String message) {
            OcrResult result = new OcrResult();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }
    }

    @lombok.Data
    public static class OcrWord {
        private String words;
        private double probability;
    }

    /**
     * 识别老谱图片，返回文字
     */
    public OcrResult recognize(MultipartFile file) {
        if (apiKey == null || apiKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            return OcrResult.error("OCR服务未配置，请管理员配置百度OCR API");
        }
        try {
            // 1. 获取access_token
            String accessToken = getAccessToken();
            if (accessToken == null) {
                return OcrResult.error("获取access_token失败，请检查API配置");
            }

            // 2. base64编码图片
            byte[] fileBytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(fileBytes);

            // 3. 调用通用文字识别
            String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=" + accessToken;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "image=" + java.net.URLEncoder.encode(base64, StandardCharsets.UTF_8);
            HttpEntity<String> request = new HttpEntity<>(body, headers);

            JsonNode response = restTemplate.postForObject(url, request, JsonNode.class);
            if (response == null) {
                return OcrResult.error("OCR API返回为空");
            }

            if (response.has("error_code")) {
                int errorCode = response.get("error_code").asInt();
                String errorMsg = response.get("error_msg").asText();
                log.error("OCR识别失败: error_code={}, error_msg={}", errorCode, errorMsg);
                return OcrResult.error(String.format("识别失败: [%d] %s", errorCode, errorMsg));
            }

            // 解析结果
            List<OcrWord> words = new ArrayList<>();
            JsonNode wordsNode = response.get("words_result");
            if (wordsNode != null && wordsNode.isArray()) {
                for (JsonNode node : wordsNode) {
                    OcrWord word = new OcrWord();
                    word.setWords(node.get("words").asText());
                    if (node.has("probability")) {
                        word.setProbability(node.get("probability").get("average").asDouble());
                    }
                    words.add(word);
                }
            }

            log.info("OCR识别完成，识别出{}个文字块", words.size());
            return OcrResult.success(words);

        } catch (Exception e) {
            log.error("OCR识别异常", e);
            return OcrResult.error("识别异常: " + e.getMessage());
        }
    }

    /**
     * 获取百度access_token
     */
    private String getAccessToken() {
        String url = String.format("https://aip.baidubce.com/oauth/2.0/token?client_id=%s&client_secret=%s&grant_type=client_credentials",
                apiKey, secretKey);
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            if (response != null && response.has("access_token")) {
                return response.get("access_token").asText();
            }
            return null;
        } catch (Exception e) {
            log.error("获取access_token失败", e);
            return null;
        }
    }
}
