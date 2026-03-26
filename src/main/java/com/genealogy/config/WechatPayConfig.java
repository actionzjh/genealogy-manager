package com.genealogy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置
 * 配置示例:
 * wechatpay.app-id=your-app-id
 * wechatpay.mch-id=your-merchant-id
 * wechatpay.api-v3-key=your-api-v3-key
 * wechatpay.private-key-path=/path/to/apiclient_key.pem
 * wechatpay.cert-path=/path/to/apicert_cert.pem
 * wechatpay.notify-url=https://your-domain.com/api/payment/wechat/notify
 * wechatpay.return-url=https://your-domain.com/membership.html
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechatpay")
public class WechatPayConfig {
    private String appId;
    private String mchId;
    private String apiV3Key;
    private String privateKeyPath;
    private String certPath;
    private String notifyUrl;
    private String returnUrl;
}
