package com.genealogy.controller;

import com.genealogy.entity.PaymentOrder;
import com.genealogy.service.WechatPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/wechat")
@RequiredArgsConstructor
public class WechatPayController {

    private final WechatPayService wechatPayService;

    /**
     * 创建微信H5支付订单
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(
            @RequestParam int level,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        PaymentOrder order = wechatPayService.createOrder(userId, level);
        if (order == null) {
            result.put("success", false);
            result.put("message", "会员等级不正确");
            return ResponseEntity.ok(result);
        }
        try {
            String h5Url = wechatPayService.createH5PayOrder(order);
            if (h5Url != null) {
                result.put("success", true);
                result.put("h5Url", h5Url);
                result.put("outTradeNo", order.getOutTradeNo());
            } else {
                result.put("success", false);
                result.put("message", "创建支付订单失败");
            }
        } catch (IOException e) {
            log.error("创建微信支付订单IO异常", e);
            result.put("success", false);
            result.put("message", "创建支付订单异常: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 微信支付异步回调
     */
    @PostMapping("/notify")
    public ResponseEntity<String> notify(HttpServletRequest request) {
        try {
            // 读取请求体
            String body = request.getReader().lines()
                    .reduce((acc, fragment) -> acc + "\n" + fragment)
                    .findFirst()
                    .orElse("");

            // 获取请求头
            String signature = request.getHeader("Wechatpay-Signature");
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String nonce = request.getHeader("Wechatpay-Nonce");

            boolean result = wechatPayService.handleNotify(body, signature, timestamp, nonce);
            if (result) {
                // 微信要求返回成功
                return new ResponseEntity<>("{\"code\":0,\"message\":\"SUCCESS\"}", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{\"code\":500,\"message\":\"FAIL\"}", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            log.error("微信回调处理异常", e);
            return new ResponseEntity<>("{\"code\":500,\"message\":\"FAIL\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
