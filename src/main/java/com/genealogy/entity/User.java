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
