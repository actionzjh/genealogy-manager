package com.genealogy.controller;

import com.genealogy.entity.GenerationWord;
import com.genealogy.service.GenerationWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字辈查询控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/generation-word")
@RequiredArgsConstructor
public class GenerationWordController {

    private final GenerationWordService generationWordService;

    /**
     * 搜索字辈
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String surname,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new HashMap<>();
        Page<GenerationWord> data = generationWordService.search(surname, page, size);
        result.put("success", true);
        result.put("data", data.getContent());
        result.put("total", data.getTotalElements());
        result.put("totalPages", data.getTotalPages());
        result.put("currentPage", data.getNumber() + 1);
        return ResponseEntity.ok(result);
    }

    /**
     * 按姓氏查询所有字辈
     */
    @GetMapping("/list/{surname}")
    public ResponseEntity<Map<String, Object>> listBySurname(@PathVariable String surname) {
        Map<String, Object> result = new HashMap<>();
        List<GenerationWord> data = generationWordService.listBySurname(surname);
        result.put("success", true);
        result.put("data", data);
        result.put("total", data.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 查询指定排行的字辈
     * GET /api/generation-word/word?surname=张&position=10
     */
    @GetMapping("/word")
    public ResponseEntity<Map<String, Object>> getWord(
            @RequestParam String surname,
            @RequestParam int position) {
        Map<String, Object> result = new HashMap<>();
        String word = generationWordService.getWordAtPosition(surname, position);
        result.put("success", true);
        result.put("word", word);
        return ResponseEntity.ok(result);
    }
}
