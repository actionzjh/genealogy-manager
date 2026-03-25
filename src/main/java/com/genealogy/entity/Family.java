package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 家庭关系实体 - 一对父母和他们的子女
 * 支持复杂关系：多配偶、再婚、收养等
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
     * 父亲ID
     */
    private Long fatherId;

    /**
     * 母亲ID
     */
    private Long motherId;

    /**
     * 子女ID列表（逗号分隔，兼容旧数据）
     * 推荐使用ChildRelation表，但这里保留兼容
     */
    @Column(length = 2000)
    private String childIds;

    /**
     * 婚姻类型: married-已婚, divorced-离婚, widowed-丧偶, single-未婚同居
     */
    @Column(length = 20)
    private String marriageType = "married";

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
     * 关系类型: biological-亲生, adopted-收养, step-继子女, foster-寄养
     */
    @Column(length = 20)
    private String relationType = "biological";

    /**
     * 备注
     */
    @Column(length = 1000)
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

    /**
     * 所属用户ID
     */
    private Long userId;

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
