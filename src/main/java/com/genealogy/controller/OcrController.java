package com.genealogy.controller;

import com.genealogy.service.OcrOldGenealogyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OCR老谱识别控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrOldGenealogyService ocrService;

    /**
     * OCR识别老谱图片
     */
    @PostMapping("/recognize")
    public ResponseEntity<Map<String, Object>> recognize(
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        OcrOldGenealogyService.OcrResult ocrResult = ocrService.recognize(file);
        result.put("success", ocrResult.isSuccess());
        result.put("message", ocrResult.getMessage());
        if (ocrResult.isSuccess()) {
            // 拼接所有文字
            String fullText = ocrResult.getWords().stream()
                    .map(OcrOldGenealogyService.OcrWord::getWords)
                    .collect(Collectors.joining("\n"));
            result.put("fullText", fullText);
            result.put("words", ocrResult.getWords());
        }
        return ResponseEntity.ok(result);
    }
}
