package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 宗亲留言板 - 公开家谱留言交流
 */
@Data
@Entity
@Table(name = "family_message", indexes = {
    @Index(name = "idx_genealogyId", columnList = "genealogyId"),
    @Index(name = "idx_parentId", columnList = "genealogyId,parentId")
})
public class FamilyMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属家谱ID
     */
    private Long genealogyId;

    /**
     * 父留言ID 0表示一级留言 >0表示回复
     */
    private Long parentId;

    /**
     * 留言者姓名
     */
    @Column(length = 100)
    private String authorName;

    /**
     * 留言者联系方式 (可选)
     */
    @Column(length = 100)
    private String contact;

    /**
     * 留言内容
     */
    @Column(length = 2000)
    private String content;

    /**
     * 是否需要审核
     */
    private Boolean needApproval;

    /**
     * 是否审核通过
     */
    private Boolean approved;

    /**
     * IP地址 (记录防垃圾)
     */
    @Column(length = 50)
    private String clientIp;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (needApproval == null) {
            needApproval = true;
        }
        if (approved == null) {
            approved = false;
        }
        if (parentId == null) {
            parentId = 0L;
        }
    }
}
