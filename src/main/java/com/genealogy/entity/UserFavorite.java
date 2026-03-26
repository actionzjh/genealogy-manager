package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户收藏 - 用户关注家谱/寻根启事
 */
@Data
@Entity
@Table(name = "user_favorite", indexes = {
    @Index(name = "idx-userId-type", columnList = "userId,type"),
    @Index(name = "idx-userId-targetId", columnList = "userId,targetId")
})
public class UserFavorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏类型: genealogy - 家谱, root-search - 寻根启事
     */
    @Column(length = 20)
    private String type;

    /**
     * 目标ID (家谱ID / 寻根启事ID)
     */
    private Long targetId;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
