package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 家族迁徙记录 - 记录家族迁徙历史，地图可视化展示
 */
@Data
@Entity
@Table(name = "family_migration", indexes = {
    @Index(name = "idx_genealogyId", columnList = "genealogyId")
})
public class FamilyMigration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属家谱ID
     */
    private Long genealogyId;

    /**
     * 迁徙事件名称
     */
    @Column(length = 200)
    private String name;

    /**
     * 迁徙描述
     */
    @Column(length = 1000)
    private String description;

    /**
     * 出发地点 纬度
     */
    private Double fromLat;

    /**
     * 出发地点 经度
     */
    private Double fromLng;

    /**
     * 出发地点 名称
     */
    @Column(length = 200)
    private String fromPlace;

    /**
     * 目的地点 纬度
     */
    private Double toLat;

    /**
     * 目的地点 经度
     */
    private Double toLng;

    /**
     * 目的地点名称
     */
    @Column(length = 200)
    private String toPlace;

    /**
     * 迁徙年份
     */
    @Column(length = 20)
    private String year;

    /**
     * 排序（按时间顺序）
     */
    private Integer sortOrder;

    /**
     * 是否公开
     */
    private Boolean isPublic;

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
