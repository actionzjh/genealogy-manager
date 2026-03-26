package com.genealogy.service;

import com.genealogy.config.CloudStorageConfig;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 七牛云存储服务 - 图片上传存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudStorageService {

    private final CloudStorageConfig config;

    /**
     * 上传结果
     */
    @Data
    public static class UploadResult {
        private boolean success;
        private String message;
        private String url;
        private String fileKey;
    }

    /**
     * 上传图片
     */
    public UploadResult uploadImage(MultipartFile file) {
        if (config.getAccessKey() == null || config.getAccessKey().isEmpty()) {
            UploadResult result = new UploadResult();
            result.setSuccess(false);
            result.setMessage("云存储未配置，请管理员配置七牛云");
            return result;
        }

        try {
            // 生成fileKey: 日期/UUID.扩展名
            String originalName = file.getOriginalFilename();
            String ext = getExtension(originalName);
            String fileKey = "genealogy/" + UUID.randomUUID().toString() + (ext != null ? "." + ext : "");

            // 上传
            Configuration cfg = new Configuration(Region.autoRegion());
            UploadManager uploadManager = new UploadManager(cfg);
            Auth auth = Auth.create(config.getAccessKey(), config.getSecretKey());
            String upToken = auth.uploadToken(config.getBucket());

            Response response = uploadManager.put(file.getBytes(), fileKey, upToken);
            if (response.isOK()) {
                String fullUrl = config.getDomain().endsWith("/")
                        ? config.getDomain() + fileKey
                        : config.getDomain() + "/" + fileKey;

                UploadResult result = new UploadResult();
                result.setSuccess(true);
                result.setMessage("上传成功");
                result.setFileKey(fileKey);
                result.setUrl(fullUrl);
                log.info("图片上传成功: {}", fullUrl);
                return result;
            } else {
                log.error("上传失败: {} {}", response.statusCode, response.error);
                UploadResult result = new UploadResult();
                result.setSuccess(false);
                result.setMessage("上传失败: " + response.error);
                return result;
            }
        } catch (IOException e) {
            log.error("上传异常", e);
            UploadResult result = new UploadResult();
            result.setSuccess(false);
            result.setMessage("上传异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
