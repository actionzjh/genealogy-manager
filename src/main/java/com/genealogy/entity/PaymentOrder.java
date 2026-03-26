package com.genealogy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单 - 会员充值订单记录
 */
@Data
@Entity
@Table(name = "payment_order", indexes = {
    @Index(name = "idx_userId_status", columnList = "userId,status"),
    @Index(name = "outTradeNo", columnList = "outTradeNo", unique = true)
})
public class PaymentOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单号
     */
    @Column(length = 64, unique = true, nullable = false)
    private String outTradeNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会员等级
     */
    private Integer membershipLevel; // 1-基础会员 2-高级会员

    /**
     * 订单金额（元）
     */
    private BigDecimal amount;

    /**
     * 订单标题
     */
    @Column(length = 200)
    private String subject;

    /**
     * 支付方式: alipay / wechat
     */
    @Column(length = 20)
    private String paymentMethod;

    /**
     * 交易状态: CREATED / PAID / CANCELLED / CLOSED
     */
    @Column(length = 20)
    private String status;

    /**
     * 支付宝/微信交易号
     */
    @Column(length = 64)
    private String tradeNo;

    /**
     * 购买时长（月）
     */
    private Integer months;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 支付时间
     */
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "CREATED";
        }
    }
}
