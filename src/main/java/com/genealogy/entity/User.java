package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体 - 系统用户
 */
@Data
@Entity
@Table(name = "user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名 - 登录用
     */
    @Column(length = 50, nullable = false, unique = true)
    private String username;

    /**
     * 邮箱 - 用于找回密码
     */
    @Column(length = 100, unique = true)
    private String email;

    /**
     * 密码哈希
     */
    @Column(length = 200, nullable = false)
    private String passwordHash;

    /**
     * 昵称/显示名称
     */
    @Column(length = 100)
    private String displayName;

    /**
     * 头像URL
     */
    @Column(length = 500)
    private String avatarUrl;

    /**
     * 账户状态: active-启用, inactive-禁用
     */
    @Column(length = 20)
    private String status = "active";

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
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 会员等级:
     * FREE - 免费版（默认）
     * BASIC - 基础会员
     * PREMIUM - 高级会员
     */
    @Column(length = 20)
    private String membershipLevel = "FREE";

    /**
     * 会员过期时间
     */
    private LocalDateTime membershipExpireAt;

    /**
     * 最大家谱数量
     */
    private Integer maxGenealogies;

    /**
     * 单家谱最大人物数量
     */
    private Integer maxPersonsPerGenealogy;

    /**
     * 最大协作者数量
     */
    private Integer maxCollaborators;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        // 默认免费版额度
        if (membershipLevel == null) {
            membershipLevel = "FREE";
        }
        if (maxGenealogies == null) {
            maxGenealogies = 1; // 免费版只能建1个家谱
        }
        if (maxPersonsPerGenealogy == null) {
            maxPersonsPerGenealogy = 500; // 免费版最多500人
        }
        if (maxCollaborators == null) {
            maxCollaborators = 2; // 免费版最多2个协作者
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
