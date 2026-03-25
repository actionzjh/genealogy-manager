package com.genealogy.controller;

import com.genealogy.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Excel导入控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ExcelImportController {

    private final ExcelImportService excelImportService;

    /**
     * 下载Excel导入模板
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            excelImportService.writeTemplate(outputStream);
            byte[] bytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "家谱导入模板.xlsx");

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("生成模板失败", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 导入家谱Excel
     */
    @PostMapping("/genealogy")
    public ResponseEntity<Map<String, Object>> importGenealogy(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件为空");
            return ResponseEntity.badRequest().body(result);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            result.put("success", false);
            result.put("message", "只支持.xlsx或.xls格式的Excel文件");
            return ResponseEntity.badRequest().body(result);
        }

        ExcelImportService.ImportResult importResult = excelImportService.importGenealogyFromExcel(file);

        if (importResult.hasErrors() && importResult.getPersonCount() == 0) {
            result.put("success", false);
            result.put("message", importResult.getErrors().get(0));
            result.put("errors", importResult.getErrors());
            return ResponseEntity.ok(result);
        }

        result.put("success", true);
        result.put("message", String.format("导入成功！家谱：%d 个，人物：%d 条",
                importResult.getGenealogyCount(), importResult.getPersonCount()));
        result.put("genealogyId", importResult.getGenealogyId());
        result.put("genealogyCount", importResult.getGenealogyCount());
        result.put("personCount", importResult.getPersonCount());
        result.put("errors", importResult.getErrors());

        return ResponseEntity.ok(result);
    }

    /**
     * 检查导入服务是否可用
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("service", "excel-import");
        return ResponseEntity.ok(result);
    }
}
