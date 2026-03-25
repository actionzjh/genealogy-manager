package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 地点实体 - 统一管理地点信息，支持行政区划层级
 */
@Data
@Entity
@Table(name = "place")
public class Place {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 地点名称
     */
    @Column(length = 200, nullable = false)
    private String name;

    /**
     * 全称（包含上级行政区）
     */
    @Column(length = 500)
    private String fullName;

    /**
     * 上级地点ID
     */
    private Long parentId;

    /**
     * 行政区划等级: country-国家, province-省/州, city-市, county-县/区, town-镇/乡, village-村
     */
    @Column(length = 20)
    private String level;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 历史地名标记 - 是否为已改名的历史地名
     */
    private Boolean isHistorical = false;

    /**
     * 现代对应地点ID
     */
    private Long modernPlaceId;

    /**
     * 备注
     */
    @Column(length = 1000)
    private String remark;

    /**
     * 所属用户ID
     */
    private Long userId;

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
