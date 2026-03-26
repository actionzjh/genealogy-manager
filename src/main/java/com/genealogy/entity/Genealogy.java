package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 家谱实体 - 一个家谱对应一个家族
 */
@Data
@Entity
@Table(name = "genealogy", indexes = {
    @Index(name = "idx_userId", columnList = "userId")
})
public class Genealogy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 家谱名称
     */
    @Column(length = 200, nullable = false)
    private String name;

    /**
     * 姓氏
     */
    @Column(length = 100)
    private String surname;

    /**
     * 始祖ID
     */
    private Long founderId;

    /**
     * 家谱描述
     */
    @Column(length = 2000)
    private String description;

    /**
     * 起源地
     */
    @Column(length = 200)
    private String originPlace;

    /**
     * 总人数
     */
    private Integer totalPeople;

    /**
     * 最大世代数
     */
    private Integer maxGeneration;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 所属用户ID
     */
    private Long userId;

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
