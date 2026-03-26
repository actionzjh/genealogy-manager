package com.genealogy.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * AI OCR 结构化识别结果记录
 */
@Entity
@Table(name = "ai_ocr_result", indexes = {
        @Index(name = "idx_userId", columnList = "userId"),
        @Index(name = "idx_genealogyId", columnList = "genealogyId")
})
public class AiOcrResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 所属家谱ID
     */
    private Long genealogyId;

    /**
     * 原始OCR文本
     */
    @Column(columnDefinition = "TEXT")
    private String originalText;

    /**
     * 结构化识别结果(JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String structuredJson;

    /**
     * 识别状态: processing / done / failed
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "processing";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGenealogyId() {
        return genealogyId;
    }

    public void setGenealogyId(Long genealogyId) {
        this.genealogyId = genealogyId;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getStructuredJson() {
        return structuredJson;
    }

    public void setStructuredJson(String structuredJson) {
        this.structuredJson = structuredJson;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
