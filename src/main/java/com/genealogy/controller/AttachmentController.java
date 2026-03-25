package com.genealogy.controller;

import com.genealogy.entity.Attachment;
import com.genealogy.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attachment")
@CrossOrigin(origins = "*")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                      @RequestParam Long genealogyId,
                                                      @RequestParam(required = false) Long personId,
                                                      @RequestParam(required = false) String bizType,
                                                      @RequestParam(required = false) String description,
                                                      @RequestParam(required = false) Integer sortOrder) {
        Map<String, Object> result = new HashMap<>();
        try {
            Attachment saved = attachmentService.store(genealogyId, personId, bizType, description, sortOrder, file);
            result.put("code", 0);
            result.put("message", "上传成功");
            result.put("data", saved);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<Map<String, Object>> findByPerson(@PathVariable Long personId) {
        Map<String, Object> result = new HashMap<>();
        List<Attachment> list = attachmentService.findByPersonId(personId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/genealogy/{genealogyId}")
    public ResponseEntity<Map<String, Object>> findByGenealogy(@PathVariable Long genealogyId) {
        Map<String, Object> result = new HashMap<>();
        List<Attachment> list = attachmentService.findByGenealogyId(genealogyId);
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Optional<Attachment> attachment = attachmentService.findById(id);
        if (attachment.isEmpty()) {
            result.put("code", 404);
            result.put("message", "附件不存在");
            return ResponseEntity.status(404).body(result);
        }
        result.put("code", 0);
        result.put("data", attachment.get());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Optional<Attachment> attachmentOptional = attachmentService.findById(id);
        if (attachmentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Attachment attachment = attachmentOptional.get();
        Resource resource = attachmentService.loadAsResource(attachment);
        String contentType = attachment.getFileType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(attachment.getFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            attachmentService.deleteById(id);
            result.put("code", 0);
            result.put("message", "删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
