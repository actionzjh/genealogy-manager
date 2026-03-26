package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作日志 - 记录用户操作，方便溯源
 */
@Data
@Entity
@Table(name = "operation_log", indexes = {
    @Index(name = "idx_userGenealogy", columnList = "userId,genealogyId")
})
public class OperationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 家谱ID
     */
    private Long genealogyId;

    /**
     * 操作对象类型：person/genealogy/collaborator/activity
     */
    @Column(length = 20)
    private String targetType;

    /**
     * 操作对象ID
     */
    private Long targetId;

    /**
     * 操作类型：create/update/delete/invite/remove/rollback
     */
    @Column(length = 20)
    private String operationType;

    /**
     * 操作描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 请求IP
     */
    @Column(length = 50)
    private String ipAddress;

    /**
     * 操作时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
