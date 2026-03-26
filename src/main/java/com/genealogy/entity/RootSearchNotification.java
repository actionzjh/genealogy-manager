package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 寻根匹配通知 - 当有新的匹配结果时通知用户
 */
@Data
@Entity
@Table(name = "root_search_notification", indexes = {
    @Index(name = "idx_userId_read", columnList = "userId,isRead")
})
public class RootSearchNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 被通知用户ID
     */
    private Long userId;

    /**
     * 寻根启事ID
     */
    private Long searchId;

    /**
     * 匹配到的家谱ID
     */
    private Long matchedGenealogyId;

    /**
     * 匹配分数
     */
    private Double matchScore;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }
}
