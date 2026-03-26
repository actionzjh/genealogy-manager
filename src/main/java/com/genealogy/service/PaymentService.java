package com.genealogy.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.genealogy.config.AlipayConfig;
import com.genealogy.entity.PaymentOrder;
import com.genealogy.entity.User;
import com.genealogy.repository.PaymentOrderRepository;
import com.genealogy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 支付服务 - 支付宝对接
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final UserRepository userRepository;
    private final AlipayConfig alipayConfig;

    /**
     * 会员定价配置
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MembershipPlan {
        private int level;
        private String name;
        private BigDecimal price;
        private int months;
        private int maxGenealogy;
        private int maxPeoplePerGenealogy;
        private int maxCollaborators;
    }

    /**
     * 获取会员套餐列表
     */
    public static List<MembershipPlan> getMembershipPlans() {
        return List.of(
            new MembershipPlan(
                1, "基础会员", new BigDecimal("99"), 12,
                10, 500, 5
            ),
            new MembershipPlan(
                2, "高级会员", new BigDecimal("299"), 12,
                999, 9999, 99
            )
        );
    }

    /**
     * 创建支付订单
     */
    @Transactional
    public PaymentOrder createOrder(Long userId, int level, String paymentMethod) {
        // 找到对应的套餐
        MembershipPlan plan = getMembershipPlans().stream()
                .filter(p -> p.getLevel() == level)
                .findFirst()
                .orElse(null);
        if (plan == null) {
            return null;
        }

        // 生成订单号
        String outTradeNo = "GZ" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 创建订单
        PaymentOrder order = new PaymentOrder();
        order.setOutTradeNo(outTradeNo);
        order.setUserId(userId);
        order.setMembershipLevel(level);
        order.setAmount(plan.getPrice());
        order.setMonths(plan.getMonths());
        order.setSubject(plan.getName() + " - " + plan.getMonths() + "个月");
        order.setPaymentMethod(paymentMethod);
        order.setStatus("CREATED");

        return paymentOrderRepository.save(order);
    }

    /**
     * 生成支付宝H5支付表单
     */
    public String createAlipayPayForm(PaymentOrder order) throws AlipayApiException {
        if (alipayConfig.getAppId() == null || alipayConfig.getAppId().isEmpty()) {
            throw new AlipayApiException("支付宝未配置，请管理员配置后再试");
        }

        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json",
                "UTF-8",
                alipayConfig.getPublicKey(),
                "RSA2"
        );

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(alipayConfig.getReturnUrl());
        request.setNotifyUrl(alipayConfig.getNotifyUrl());

        // 构造请求参数
        StringBuilder bizContent = new StringBuilder();
        bizContent.append("{");
        bizContent.append("\"out_trade_no\":\"").append(order.getOutTradeNo()).append("\",");
        bizContent.append("\"product_code\":\"QUICK_WAP_WAY\",");
        bizContent.append("\"total_amount\":\"").append(order.getAmount()).append("\",");
        bizContent.append("\"subject\":\"").append(order.getSubject()).append("\"");
        bizContent.append("}");
        request.setBizContent(bizContent.toString());

        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            return response.getBody();
        } else {
            log.error("创建支付宝订单失败: {}", response.getMsg());
            throw new AlipayApiException(response.getMsg());
        }
    }

    /**
     * 支付成功回调 - 更新订单状态并开通会员
     */
    @Transactional
    public boolean handleAlipayNotify(String outTradeNo, String tradeNo, String status) {
        Optional<PaymentOrder> opt = paymentOrderRepository.findByOutTradeNo(outTradeNo);
        if (opt.isEmpty()) {
            log.error("支付回调：订单不存在 {}", outTradeNo);
            return false;
        }

        PaymentOrder order = opt.get();
        if (!"CREATED".equals(order.getStatus()) && "PAID".equals(order.getStatus())) {
            log.info("订单已支付，重复回调 {}", outTradeNo);
            return true; // 已经处理过，返回成功避免重复通知
        }

        if (!"TRADE_SUCCESS".equals(status)) {
            log.info("订单未成功 {}", outTradeNo);
            return false;
        }

        // 更新订单状态
        order.setStatus("PAID");
        order.setTradeNo(tradeNo);
        order.setPaidAt(LocalDateTime.now());
        paymentOrderRepository.save(order);

        // 开通会员
        return activateMembership(order);
    }

    /**
     * 开通/延长会员
     */
    private boolean activateMembership(PaymentOrder order) {
        Optional<User> userOpt = userRepository.findById(order.getUserId());
        if (userOpt.isEmpty()) {
            log.error("开通会员：用户不存在 {}", order.getUserId());
            return false;
        }

        User user = userOpt.get();

        // 计算过期时间
        LocalDateTime expireTime;
        if (user.getMembershipExpireAt() != null && user.getMembershipExpireAt().isAfter(LocalDateTime.now())) {
            // 还没过期，续期
            expireTime = user.getMembershipExpireAt().plusMonths(order.getMonths());
        } else {
            // 新开
            expireTime = LocalDateTime.now().plusMonths(order.getMonths());
        }

        // 更新会员信息
        user.setMembershipLevel(order.getMembershipLevel());
        user.setMembershipExpireAt(expireTime);

        // 更新额度限制（根据套餐）
        MembershipPlan plan = getMembershipPlans().stream()
                .filter(p -> p.getLevel() == order.getMembershipLevel())
                .findFirst()
                .orElse(null);
        if (plan != null) {
            user.setMaxGenealogy(plan.getMaxGenealogy());
            user.setMaxPeoplePerGenealogy(plan.getMaxPeoplePerGenealogy());
            user.setMaxCollaborators(plan.getMaxCollaborators());
        }

        userRepository.save(user);
        log.info("会员开通成功: userId={}, level={}, expire={}",
                order.getUserId(), order.getMembershipLevel(), expireTime);
        return true;
    }

    /**
     * 查询用户订单列表
     */
    public List<PaymentOrder> listUserOrders(Long userId) {
        return paymentOrderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
