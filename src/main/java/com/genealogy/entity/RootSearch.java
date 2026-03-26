package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 寻根启事实ent - 用户发布寻根信息
 */
@Data
@Entity
@Table(name = "root_search", indexes = {
    @Index(name = "idx_surname_status", columnList = "surname,status,isPublic"),
    @Index(name = "idx_userId", columnList = "userId")
})
public class RootSearch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发布用户ID
     */
    private Long userId;

    /**
     * 姓氏
     */
    @Column(length = 100)
    private String surname;

    /**
     * 始迁祖姓名
     */
    @Column(length = 200)
    private String ancestorName;

    /**
     * 迁出地（祖籍）
     */
    @Column(length = 200)
    private String originPlace;

    /**
     * 迁入地（现居）
     */
    @Column(length = 200)
    private String currentPlace;

    /**
     * 已知字辈（空格分隔）
     */
    @Column(length = 500)
    private String generations;

    /**
     * 详细说明
     */
    @Column(length = 2000)
    private String description;

    /**
     * 联系邮箱/联系方式
     */
    @Column(length = 200)
    private String contact;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 状态：open-开启中 closed-已找到
     */
    @Column(length = 20)
    private String status;

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
            status = "open";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
