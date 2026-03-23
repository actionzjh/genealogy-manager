package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 家庭关系实体 - 一对父母和他们的子女
 */
@Data
@Entity
@Table(name = "family")
public class Family {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 家谱ID
     */
    private Long genealogyId;

    /**
     * 丈夫ID
     */
    private Long husbandId;

    /**
     * 妻子ID
     */
    private Long wifeId;

    /**
     * 结婚日期
     */
    @Column(length = 50)
    private String marriageDate;

    /**
     * 离婚日期
     */
    @Column(length = 50)
    private String divorceDate;

    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
