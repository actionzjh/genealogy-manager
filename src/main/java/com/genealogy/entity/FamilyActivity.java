package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 家族活动实体 - 家族宣传发布的宗亲活动
 */
@Data
@Entity
@Table(name = "family_activity", indexes = {
    @Index(name = "idx_genealogyId_public", columnList = "genealogyId,isPublic")
})
public class FamilyActivity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属家谱ID
     */
    private Long genealogyId;

    /**
     * 活动标题
     */
    @Column(length = 200)
    private String title;

    /**
     * 活动描述/详情
     */
    @Column(length = 5000)
    private String description;

    /**
     * 活动时间
     */
    private LocalDate activityDate;

    /**
     * 活动地点
     */
    @Column(length = 200)
    private String location;

    /**
     * 活动类型：meeting-宗亲会/ancestor-worship-祭祖/construction-修祠/other-其他
     */
    @Column(length = 20)
    private String type;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 活动状态：planned-筹备中/ongoing-进行中/finished-已结束
     */
    @Column(length = 20)
    private String status;

    /**
     * 封面图片URL
     */
    @Column(length = 500)
    private String coverImage;

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
        if (status == null) {
            status = "planned";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
