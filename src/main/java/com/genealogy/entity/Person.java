package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * 人物实体 - 家谱核心数据
 */
@Data
@Entity
@Table(name = "person")
public class Person {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 姓名
     */
    @Column(length = 100, nullable = false)
    private String name;

    /**
     * 字
     */
    @Column(length = 100)
    private String styleName;

    /**
     * 号
     */
    @Column(length = 100)
    private String hao;

    /**
     * 性别: M-男, F-女, U-未知
     */
    @Column(length = 10)
    private String gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 逝世日期
     */
    private LocalDate deathDate;

    /**
     * 出生日期（公元纪年，字符串形式，支持公元前）
     */
    @Column(length = 50)
    private String birthYear;

    /**
     * 逝世日期（公元纪年，字符串形式）
     */
    @Column(length = 50)
    private String deathYear;

    /**
     * 父亲ID
     */
    private Long fatherId;

    /**
     * 母亲ID
     */
    private Long motherId;

    /**
     * 配偶ID列表，多个用逗号分隔
     */
    @Column(length = 500)
    private String spouseIds;

    /**
     * 支系名称
     */
    @Column(length = 200)
    private String branch;

    /**
     * 迁徙路线
     */
    @Column(length = 500)
    private String migrationPath;

    /**
     * 主要功绩/称号
     */
    @Column(length = 1000)
    private String achievements;

    /**
     * 人物传记
     */
    @Column(length = 5000)
    private String biography;

    /**
     * 传记来源
     */
    @Column(length = 200)
    private String source;

    /**
     * 墓地位置
     */
    @Column(length = 500)
    private String cemeteryLocation;

    /**
     * 封号/爵位
     */
    @Column(length = 200)
    private String title;

    /**
     * 官职/职称
     */
    @Column(length = 200)
    private String occupation;

    /**
     * 婚姻状况
     */
    @Column(length = 50)
    private String maritalStatus;

    /**
     * 状态: alive-在世, deceased-去世
     */
    @Column(length = 50)
    private String status;

    /**
     * 世代代数（从始祖开始算）
     */
    private Integer generation;

    /**
     * 排序序号，用于世系图排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private java.time.LocalDateTime updatedAt;

    /**
     * 所属用户ID
     */
    private Long userId;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}
