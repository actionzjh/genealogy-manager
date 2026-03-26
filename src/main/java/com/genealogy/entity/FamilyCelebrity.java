package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 家族名人 - 展示家族杰出人物
 */
@Data
@Entity
@Table(name = "family_celebrity", indexes = {
    @Index(name = "idx_genealogyId", columnList = "genealogyId"),
    @Index(name = "idx_genealogyId_isPublic", columnList = "genealogyId,isPublic")
})
public class FamilyCelebrity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属家谱ID
     */
    private Long genealogyId;

    /**
     * 名人姓名
     */
    @Column(length = 100)
    private String name;

    /**
     * 字
     */
    @Column(length = 100)
    private String styleName;

    /**
     * 号
     */
    @Column(length = 100)
    private String hao;

    /**
     * 出生年份
     */
    @Column(length = 20)
    private String birthYear;

    /**
     * 逝世年份
     */
    @Column(length = 20)
    private String deathYear;

    /**
     * 出生地
     */
    @Column(length = 200)
    private String birthPlace;

    /**
     * 身份/官职/成就
     */
    @Column(length = 200)
    private String title;

    /**
     * 详细生平简介
     */
    @Column(length = 5000)
    private String biography;

    /**
     * 头像/照片URL
     */
    @Column(length = 500)
    private String photoUrl;

    /**
     * 辈分/世代
     */
    private Integer generation;

    /**
     * 是否公开展示
     */
    private Boolean isPublic;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建用户ID
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
