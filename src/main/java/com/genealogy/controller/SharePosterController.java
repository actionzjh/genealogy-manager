package com.genealogy.controller;

import com.genealogy.service.SharePosterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 分享海报生成控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class SharePosterController {

    private final SharePosterService posterService;

    /**
     * 生成家族分享海报
     */
    @GetMapping("/poster")
    public ResponseEntity<byte[]> generatePoster(@RequestParam String genealogyName, @RequestParam String url) {
        try {
            byte[] imageBytes = posterService.generatePoster(genealogyName, url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(imageBytes.length);
            // 缓存一小时
            headers.setCacheControl("max-age=3600");

            return ResponseEntity.ok().headers(headers).body(imageBytes);
        } catch (IOException e) {
            log.error("生成海报失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
