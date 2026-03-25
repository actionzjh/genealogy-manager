package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 姓氏百科实体 - 存储姓氏起源、郡望堂号等信息
 */
@Data
@Entity
@Table(name = "surname")
public class Surname {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 姓氏
     */
    @Column(length = 50, nullable = false, unique = true)
    private String name;

    /**
     * 姓氏拼音
     */
    @Column(length = 100)
    private String pinyin;

    /**
     * 起源
     */
    @Column(length = 5000)
    private String origin;

    /**
     * 郡望
     */
    @Column(length = 2000)
    private String junwang;

    /**
     * 堂号
     */
    @Column(length = 2000)
    private String tanghao;

    /**
     * 姓氏名人
     */
    @Column(length = 3000)
    private String famousPeople;

    /**
     * 迁徙分布
     */
    @Column(length = 2000)
    private String migration;

    /**
     * 家谱数量
     */
    private Integer genealogyCount = 0;

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
        if (genealogyCount == null) {
            genealogyCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
