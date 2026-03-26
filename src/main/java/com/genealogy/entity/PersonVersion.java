package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 人物信息历史版本 - 用于版本回滚
 */
@Data
@Entity
@Table(name = "person_version")
public class PersonVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 人物ID
     */
    private Long personId;

    /**
     * 家谱ID
     */
    private Long genealogyId;

    /**
     * 修改人用户ID
     */
    private Long modifierUserId;

    /**
     * 修改前人物数据 JSON快照
     */
    @Column(length = 10000)
    private String beforeSnapshot;

    /**
     * 修改后人物数据 JSON快照
     */
    @Column(length = 10000)
    private String afterSnapshot;

    /**
     * 修改说明
     */
    @Column(length = 200)
    private String changeDescription;

    /**
     * 修改时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
