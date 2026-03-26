package com.genealogy.controller;

import com.alipay.api.AlipayApiException;
import com.genealogy.entity.PaymentOrder;
import com.genealogy.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 获取会员套餐列表
     */
    @GetMapping("/plans")
    public ResponseEntity<Map<String, Object>> getPlans() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", PaymentService.getMembershipPlans());
        return ResponseEntity.ok(result);
    }

    /**
     * 创建支付宝订单，返回支付表单
     */
    @PostMapping("/alipay/create")
    public void createAlipayOrder(
            @RequestParam int level,
            Authentication authentication,
            HttpServletResponse response) throws IOException {
        if (authentication == null || authentication.getPrincipal() == null) {
            response.sendError(401, "请先登录");
            return;
        }
        Long userId = (Long) authentication.getPrincipal();
        PaymentOrder order = paymentService.createOrder(userId, level, "alipay");
        if (order == null) {
            response.sendError(400, "会员等级不正确");
            return;
        }
        try {
            String form = paymentService.createAlipayPayForm(order);
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.write(form);
            out.flush();
        } catch (AlipayApiException e) {
            log.error("创建支付宝订单失败", e);
            response.sendError(500, e.getMessage());
        }
    }

    /**
     * 支付宝异步回调
     */
    @PostMapping("/alipay/notify")
    public void alipayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            params.put(key, values[0]);
        });

        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

        boolean result = paymentService.handleAlipayNotify(outTradeNo, tradeNo, tradeStatus);
        if (result) {
            response.getWriter().write("success");
        } else {
            response.getWriter().write("failure");
        }
    }

    /**
     * 查询用户订单列表
     */
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> listOrders(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        Long userId = (Long) authentication.getPrincipal();
        List<PaymentOrder> orders = paymentService.listUserOrders(userId);
        result.put("success", true);
        result.put("data", orders);
        return ResponseEntity.ok(result);
    }
}
