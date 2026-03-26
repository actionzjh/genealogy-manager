package com.genealogy.repository;

import com.genealogy.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 支付订单 Repository
 */
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long>,
        JpaSpecificationExecutor<PaymentOrder> {

    /**
     * 根据订单号查询
     */
    Optional<PaymentOrder> findByOutTradeNo(String outTradeNo);

    /**
     * 查询用户订单列表
     */
    List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
}
