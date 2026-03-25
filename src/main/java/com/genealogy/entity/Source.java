package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 来源引用实体 - 记录信息来源（族谱、墓碑、口述等）
 */
@Data
@Entity
@Table(name = "source")
public class Source {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 家谱ID
     */
    private Long genealogyId;

    /**
     * 来源标题
     */
    @Column(length = 200, nullable = false)
    private String title;

    /**
     * 来源类型: book-书籍, manuscript-手稿, tombstone-墓碑, oral-口述, record-档案, photo-照片, other-其他
     */
    @Column(length = 30)
    private String sourceType;

    /**
     * 作者/编纂者
     */
    @Column(length = 100)
    private String author;

    /**
     * 出版日期
     */
    @Column(length = 50)
    private String publicationDate;

    /**
     * 出版地点
     */
    @Column(length = 200)
    private String publicationPlace;

    /**
     * 页码/卷期
     */
    @Column(length = 50)
    private String pageInfo;

    /**
     * 存储位置（图书馆/档案馆等）
     */
    @Column(length = 200)
    private String repository;

    /**
     * 备注/描述
     */
    @Column(length = 2000)
    private String description;

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
