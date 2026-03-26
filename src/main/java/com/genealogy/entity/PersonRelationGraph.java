package com.genealogy.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 血缘关系可视化视图实体
 * 保存用户保存的自定义关系视图配置
 */
@Entity
@Table(name = "person_relation_graph", indexes = {
        @Index(name = "idx_genealogyId", columnList = "genealogyId")
})
public class PersonRelationGraph {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属家谱ID
     */
    private Long genealogyId;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 视图名称
     */
    private String name;

    /**
     * 中心人物ID（起点人物）
     */
    private Long centerPersonId;

    /**
     * 拓展深度（几代人）
     */
    private Integer depth;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isPublic == null) {
            isPublic = false;
        }
        if (depth == null) {
            depth = 3;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGenealogyId() {
        return genealogyId;
    }

    public void setGenealogyId(Long genealogyId) {
        this.genealogyId = genealogyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCenterPersonId() {
        return centerPersonId;
    }

    public void setCenterPersonId(Long centerPersonId) {
        this.centerPersonId = centerPersonId;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
