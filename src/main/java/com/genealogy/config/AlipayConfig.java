package com.genealogy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付配置
 * 配置在 application.properties:
 * alipay.app-id=your-app-id
 * alipay.private-key=your-private-key
 * alipay.public-key=your-alipay-public-key
 * alipay.gateway-url=https://openapi.alipaydev.com/gateway.do (沙箱)
 * alipay.notify-url=https://your-domain.com/api/payment/alipay/notify
 * alipay.return-url=https://your-domain.com/membership.html
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    private String appId;
    private String privateKey;
    private String publicKey;
    private String gatewayUrl;
    private String notifyUrl;
    private String returnUrl;
}
