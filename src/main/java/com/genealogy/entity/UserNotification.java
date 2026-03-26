package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户系统通知 - 收藏内容更新/寻根匹配通知
 */
@Data
@Entity
@Table(name = "user_notification", indexes = {
    @Index(name = "idx_userId_isRead", columnList = "userId,isRead")
})
public class UserNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 通知类型:
     * MATCH - 寻根匹配成功
     * UPDATE - 收藏内容更新
     * COMMENT - 有新留言回复
     * SYSTEM - 系统通知
     */
    @Column(length = 20)
    private String type;

    /**
     * 通知标题
     */
    @Column(length = 200)
    private String title;

    /**
     * 通知内容
     */
    @Column(length = 500)
    private String content;

    /**
     * 关联目标类型
     */
    @Column(length = 20)
    private String targetType;

    /**
     * 关联目标ID
     */
    private Long targetId;

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
