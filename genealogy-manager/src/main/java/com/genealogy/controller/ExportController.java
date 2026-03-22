package com.genealogy.controller;

import com.genealogy.service.PersonService;
import com.genealogy.service.FamilyService;
import com.genealogy.util.GedcomExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 导出接口 - 支持导出GEDCOM等格式
 */
@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class ExportController {

    @Autowired
    private PersonService personService;

    @Autowired
    private FamilyService familyService;

    /**
     * 导出为GEDCOM格式
     */
    @GetMapping("/gedcom")
    public ResponseEntity<byte[]> exportGedcom(
            @RequestParam Long genealogyId,
            @RequestParam(defaultValue = "genealogy") String title) {
        try {
            GedcomExporter exporter = new GedcomExporter(personService, familyService);
            String content = exporter.export(genealogyId, title);
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormits("attachment", title + ".ged");
            headers.setContentLength(bytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取导出统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("totalPeople", personService.count());
        result.put("totalFamilies", familyService.count());
        return ResponseEntity.ok(result);
    }
}
