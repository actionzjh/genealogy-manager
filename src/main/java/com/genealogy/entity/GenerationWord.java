package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字辈诗词 - 存储不同姓氏不同支派的字辈排行，对外提供查询
 */
@Data
@Entity
@Table(name = "generation_word", indexes = {
    @Index(name = "idx_surname", columnList = "surname")
})
public class GenerationWord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 姓氏
     */
    @Column(length = 100)
    private String surname;

    /**
     * 支派名称
     */
    @Column(length = 200)
    private String branch;

    /**
     * 祖籍
     */
    @Column(length = 200)
    private String originPlace;

    /**
     * 字辈排行，每个字空格分隔
     */
    @Column(length = 500)
    private String words;

    /**
     * 说明/源流
     */
    @Column(length = 2000)
    private String description;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 所属家谱ID（如果有）
     */
    private Long genealogyId;

    /**
     * 创建用户ID
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
        if (isPublic == null) {
            isPublic = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
