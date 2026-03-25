package com.genealogy.controller;

import com.genealogy.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PDF导出控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class PdfExportController {

    private final PdfExportService pdfExportService;

    /**
     * 获取所有可用的排版格式
     */
    @GetMapping("/layouts")
    public ResponseEntity<Map<String, Object>> getAvailableLayouts() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("layouts", pdfExportService.getAvailableLayouts());
        return ResponseEntity.ok(result);
    }

    /**
     * 导出家谱PDF，指定排版格式
     * GET /api/export/genealogy/{genealogyId}?layout=table-list
     */
    @GetMapping("/genealogy/{genealogyId}")
    public ResponseEntity<byte[]> exportGenealogyPdf(
            @PathVariable Long genealogyId,
            @RequestParam(required = false) String layout) {
        try {
            ByteArrayOutputStream outputStream;
            if (layout != null && pdfExportService.hasLayout(layout)) {
                outputStream = pdfExportService.exportGenealogyToPdf(genealogyId, layout);
            } else {
                outputStream = pdfExportService.exportGenealogyToPdf(genealogyId);
            }
            byte[] bytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "genealogy-" + genealogyId;
            if (layout != null) {
                filename += "-" + layout;
            }
            filename += ".pdf";
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("导出失败: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("PDF导出失败", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取导出链接（前端页面用）
     */
    @PostMapping("/genealogy/{genealogyId}")
    public ResponseEntity<Map<String, Object>> getExportUrl(
            @PathVariable Long genealogyId,
            @RequestParam(required = false) String layout) {
        Map<String, Object> result = new HashMap<>();
        String downloadUrl = "/api/export/genealogy/" + genealogyId;
        if (layout != null && !layout.isEmpty()) {
            downloadUrl += "?layout=" + layout;
        }
        result.put("success", true);
        result.put("downloadUrl", downloadUrl);
        result.put("availableLayouts", pdfExportService.getAvailableLayouts());
        return ResponseEntity.ok(result);
    }

    /**
     * 检查导出服务是否可用
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("service", "pdf-export");
        result.put("availableLayouts", pdfExportService.getAvailableLayouts());
        return ResponseEntity.ok(result);
    }
}
