package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 家族相册 - 存放家族老照片、活动照片
 */
@Data
@Entity
@Table(name = "family_photo", indexes = {
    @Index(name = "idx_genealogyId", columnList = "genealogyId")
})
public class FamilyPhoto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属家谱ID
     */
    private Long genealogyId;

    /**
     * 照片标题
     */
    @Column(length = 200)
    private String title;

    /**
     * 照片描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 文件key (飞书/oss存储key)
     */
    @Column(length = 200)
    private String fileKey;

    /**
     * 访问URL
     */
    @Column(length = 500)
    private String url;

    /**
     * 拍摄年份
     */
    @Column(length = 10)
    private String year;

    /**
     * 拍摄地点
     */
    @Column(length = 200)
    private String location;

    /**
     * 是否公开展示
     */
    private Boolean isPublic;

    /**
     * 排序 (越大越靠前)
     */
    private Integer sortOrder;

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isPublic == null) {
            isPublic = true;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}
