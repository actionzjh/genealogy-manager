package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 家谱协作者 - 多人协作权限管理
 */
@Data
@Entity
@Table(name = "genealogy_collaborator", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"genealogyId", "userId"})
}, indexes = {
    @Index(name = "idx_genealogyUserId", columnList = "genealogyId,userId")
})
public class GenealogyCollaborator {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 家谱ID
     */
    private Long genealogyId;

    /**
     * 协作者用户ID
     */
    private Long userId;

    /**
     * 权限类型:
     * OWNER - 所有者，可管理协作者、删除家谱
     * EDITOR - 编辑者，可修改内容
     * VIEWER - 浏览者，只读
     */
    @Column(length = 20)
    private String role;

    /**
     * 邀请人用户ID
     */
    private Long inviterId;

    /**
     * 创建时间（邀请时间）
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
