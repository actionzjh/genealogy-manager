package com.genealogy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 云存储配置 - 支持七牛云
 * 配置示例:
 * cloudstorage.qiniu.access-key=your-access-key
 * cloudstorage.qiniu.secret-key=your-secret-key
 * cloudstorage.qiniu.bucket=your-bucket
 * cloudstorage.qiniu.domain=https://your-domain.com
 */
@Data
@Component
@ConfigurationProperties(prefix = "cloudstorage.qiniu")
public class CloudStorageConfig {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String domain;
}
