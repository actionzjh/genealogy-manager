package com.genealogy.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.genealogy.config.WechatPayConfig;
import com.genealogy.entity.PaymentOrder;
import com.genealogy.repository.PaymentOrderRepository;
import com.genealogy.repository.UserRepository;
import com.wechat.pay.java.contrib.validator.WechatPayValidator;
import com.wechat.pay.java.contrib.validator.exception.ValidationException;
import com.wechat.pay.java.core.RSA;
import com.wechat.pay.java.core.http.*;
import com.wechatpay.apiv3.WechatPayHttpClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 微信支付服务 - H5支付
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPayService {

    private final WechatPayConfig config;
    private final PaymentOrderRepository paymentOrderRepository;
    private final UserRepository userRepository;

    /**
     * 创建微信H5支付订单，返回h5链接
     */
    public String createH5PayOrder(PaymentOrder order) throws IOException {
        // 初始化HttpClient
        PrivateKey privateKey = RSA.pemPrivateKey(
            Files.readAllBytes(Paths.get(config.getPrivateKeyPath()))
        );
        X509Certificate wechatPayCert = RSA.pemCertificate(
            Files.readAllBytes(Paths.get(config.getCertPath()))
        );

        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(config.getMchId(), config.getApiV3Key(), privateKey)
                .withWechatPay(new X509Certificate[]{wechatPayCert});

        HttpClient httpClient = builder.build();

        // 构建请求JSON
        ObjectNode request = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        request.put("appid", config.getAppId());
        request.put("mchid", config.getMchId());
        request.put("description", order.getSubject());
        request.put("out_trade_no", order.getOutTradeNo());
        request.put("notify_url", config.getNotifyUrl());
        ObjectNode amount = request.putObject("amount");
        amount.put("total", order.getAmount().multiply(java.math.BigDecimal.valueOf(100)).intValue());
        amount.put("currency", "CNY");
        ObjectNode sceneInfo = request.putObject("scene_info");
        sceneInfo.put("payer_client_ip", "");
        // H5支付需要
        ObjectNode h5Info = sceneInfo.putObject("h5_info");
        h5Info.put("type", "Wap");
        h5Info.put("app_name", "家谱管理系统");

        // 发送请求
        HttpRequest requestObj = new HttpRequest.Builder()
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .url("https://api.mch.weixin.qq.com/v3/pay/transactions/h5")
                .post(request.toString())
                .build();

        HttpResponse response = httpClient.execute(requestObj);
        if (response.getStatusCode() == 200) {
            String body = response.getBody();
            // 解析返回h5_url
            com.fasterxml.jackson.databind.JsonNode json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
            return json.get("h5_url").asText();
        } else {
            log.error("创建微信支付订单失败: status={}, body={}", response.getStatusCode(), response.getBody());
            return null;
        }
    }

    /**
     * 处理支付回调
     */
    @Transactional
    public boolean handleNotify(String body, String signature, String timestamp, String nonce) {
        try {
            // 验证签名
            WechatPayValidator validator = new WechatPayValidator(
                    config.getApiV3Key()
            );
            if (!validator.validate(signature, timestamp, nonce, body)) {
                log.error("微信支付回调签名验证失败");
                return false;
            }

            // 解析通知
            com.fasterxml.jackson.databind.JsonNode json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
            String outTradeNo = json.get("out_trade_no").asText();
            String transactionId = json.get("transaction_id").asText();
            String tradeState = json.get("trade_state").asText();

            // 查询订单
            java.util.Optional<PaymentOrder> opt = paymentOrderRepository.findByOutTradeNo(outTradeNo);
            if (opt.isEmpty()) {
                log.error("微信回调: 订单不存在 {}", outTradeNo);
                return false;
            }

            PaymentOrder order = opt.get();
            if ("PAID".equals(order.getStatus())) {
                log.info("订单已支付，重复回调 {}", outTradeNo);
                return true;
            }

            if ("SUCCESS".equals(tradeState)) {
                // 更新订单状态
                order.setStatus("PAID");
                order.setTradeNo(transactionId);
                order.setPaidAt(LocalDateTime.now());
                paymentOrderRepository.save(order);

                // 开通会员
                activateMembership(order);
                log.info("微信支付成功，会员已开通: order={}", outTradeNo);
                return true;
            }

            return false;
        } catch (ValidationException | IOException e) {
            log.error("微信支付回调处理异常", e);
            return false;
        }
    }

    /**
     * 开通/延长会员
     */
    private void activateMembership(PaymentOrder order) {
        userRepository.findById(order.getUserId()).ifPresent(user -> {
            // 计算过期时间
            LocalDateTime expireTime;
            if (user.getMembershipExpireAt() != null && user.getMembershipExpireAt().isAfter(LocalDateTime.now())) {
                expireTime = user.getMembershipExpireAt().plusMonths(order.getMonths());
            } else {
                expireTime = LocalDateTime.now().plusMonths(order.getMonths());
            }

            // 更新会员信息
            user.setMembershipLevel(order.getMembershipLevel());
            user.setMembershipExpireAt(expireTime);

            // 更新限额
            java.util.List<PaymentService.MembershipPlan> plans = PaymentService.getMembershipPlans();
            plans.stream()
                    .filter(p -> p.getLevel() == order.getMembershipLevel())
                    .findFirst()
                    .ifPresent(plan -> {
                        user.setMaxGenealogy(plan.getMaxGenealogy());
                        user.setMaxPeoplePerGenealogy(plan.getMaxPeoplePerGenealogy());
                        user.setMaxCollaborators(plan.getMaxCollaborators());
                    });

            userRepository.save(user);
        });
    }

    /**
     * 创建订单
     */
    @Transactional
    public PaymentOrder createOrder(Long userId, int level) {
        // 获取套餐
        java.util.List<PaymentService.MembershipPlan> plans = PaymentService.getMembershipPlans();
        PaymentService.MembershipPlan plan = plans.stream()
                .filter(p -> p.getLevel() == level)
                .findFirst()
                .orElse(null);
        if (plan == null) {
            return null;
        }

        // 生成订单号
        String outTradeNo = "WX" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 创建订单
        PaymentOrder order = new PaymentOrder();
        order.setOutTradeNo(outTradeNo);
        order.setUserId(userId);
        order.setMembershipLevel(level);
        order.setAmount(plan.getPrice());
        order.setMonths(plan.getMonths());
        order.setSubject(plan.getName() + " - " + plan.getMonths() + "个月");
        order.setPaymentMethod("wechat");
        order.setStatus("CREATED");

        return paymentOrderRepository.save(order);
    }
}
