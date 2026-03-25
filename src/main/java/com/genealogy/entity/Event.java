package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 事件实体 - 人物相关事件（出生、结婚、逝世、移民等）
 */
@Data
@Entity
@Table(name = "event")
public class Event {
    
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
     * 事件类型: birth-出生, death-逝世, marriage-结婚, divorce-离婚, immigration-移民, occupation-职业变更, residence-迁居, other-其他
     */
    @Column(length = 30, nullable = false)
    private String eventType;

    /**
     * 事件日期（字符串，支持公元前）
     */
    @Column(length = 50)
    private String eventDate;

    /**
     * 事件地点
     */
    @Column(length = 200)
    private String eventPlace;

    /**
     * 坐标纬度
     */
    private Double latitude;

    /**
     * 坐标经度
     */
    private Double longitude;

    /**
     * 事件描述
     */
    @Column(length = 2000)
    private String description;

    /**
     * 来源引用
     */
    @Column(length = 200)
    private String source;

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
