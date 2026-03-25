package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 人物图片实体 - 支持多张照片
 */
@Data
@Entity
@Table(name = "person_image")
public class PersonImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 家谱ID
     */
    private Long genealogyId;

    /**
     * 人物ID
     */
    private Long personId;

    /**
     * 图片URL或文件路径
     */
    @Column(length = 500, nullable = false)
    private String imageUrl;

    /**
     * 图片说明/标题
     */
    @Column(length = 200)
    private String caption;

    /**
     * 拍摄日期
     */
    @Column(length = 50)
    private String shootDate;

    /**
     * 是否为头像
     */
    private Boolean isAvatar = false;

    /**
     * 排序序号
     */
    private Integer sortOrder = 0;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
