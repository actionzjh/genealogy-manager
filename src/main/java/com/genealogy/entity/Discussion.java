package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 讨论/寻亲公告实体 - 用户发布寻亲信息或讨论
 */
@Data
@Entity
@Table(name = "discussion")
public class Discussion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 标题
     */
    @Column(length = 200, nullable = false)
    private String title;

    /**
     * 内容
     */
    @Column(length = 5000)
    private String content;

    /**
     * 类型: search-寻亲, discussion-讨论
     */
    @Column(length = 20)
    private String type;

    /**
     * 关联家谱ID（可选）
     */
    private Long genealogyId;

    /**
     * 发布者用户ID
     */
    private Long userId;

    /**
     * 发布者昵称
     */
    @Column(length = 100)
    private String authorName;

    /**
     * 状态: open-开放, closed-已找到/已关闭
     */
    @Column(length = 20)
    private String status = "open";

    /**
     * 浏览次数
     */
    private Integer viewCount = 0;

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
        if (viewCount == null) {
            viewCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
