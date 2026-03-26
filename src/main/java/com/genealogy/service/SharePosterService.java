package com.genealogy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * 家族宣传分享海报生成服务
 * 生成带二维码的分享海报，方便微信朋友圈分享
 */
@Slf4j
@Service
public class SharePosterService {

    /**
     * 海报尺寸（手机竖屏）
     */
    private static final int WIDTH = 750;
    private static final int HEIGHT = 1334;

    /**
     * 生成家族分享海报
     * @param genealogyName 家谱名称
     * @param shareUrl 分享链接
     * @return 图片字节数组 PNG格式
     */
    public byte[] generatePoster(String genealogyName, String shareUrl) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 抗锯齿
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 背景渐变
        Paint bgPaint = new GradientPaint(0, 0, new Color(138, 75, 36), WIDTH, HEIGHT, new Color(185, 109, 59));
        g.setPaint(bgPaint);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 标题
        g.setColor(Color.WHITE);
        Font titleFont = new Font("Source Han Sans CN", Font.BOLD, 48);
        g.setFont(titleFont);
        String title = "家谱寻根";
        int titleWidth = g.getFontMetrics(titleFont).stringWidth(title);
        g.drawString(title, (WIDTH - titleWidth) / 2, 120);

        // 副标题
        Font subtitleFont = new Font("Source Han Sans CN", Font.PLAIN, 24);
        g.setFont(subtitleFont);
        String subtitle = "邀你一起寻根问祖，传承家族文化";
        int subtitleWidth =g.getFontMetrics(subtitleFont).stringWidth(subtitle);
        g.drawString(subtitle, (WIDTH - subtitleWidth) / 2, 180);

        // 家谱名称卡片
        g.setColor(new Color(255, 253, 250));
        g.fillRoundRect(50, 250, WIDTH - 100, 150, 20, 20);
        g.setColor(new Color(138, 75, 36));
        Font nameFont = new Font("Source Han Sans CN", Font.BOLD, 36);
        g.setFont(nameFont);
        int nameWidth = g.getFontMetrics(nameFont).stringWidth(genealogyName);
        g.drawString(genealogyName, (WIDTH - nameWidth) / 2, 340);

        // 提示文字
        g.setColor(Color.WHITE);
        Font tipFont = new Font("Source Han Sans CN", Font.PLAIN, 20);
        g.setFont(tipFont);
        String tip = "扫描下方二维码 查看家族主页";
        int tipWidth = g.getFontMetrics(tipFont).stringWidth(tip);
        g.drawString(tip, (WIDTH - tipWidth) / 2, 500);

        // 这里需要二维码生成，如果没有二维码库，可以留空位置提示
        // 实际使用可以集成zxing生成二维码
        g.setColor(Color.WHITE);
        g.fillRect((WIDTH - 300) / 2, 550, 300, 300);
        g.setColor(new Color(138, 75, 36));
        g.drawString("二维码", (WIDTH - g.getFontMetrics(tipFont).stringWidth("二维码")) / 2, 700);

        // 底部文案
        Font bottomFont = new Font("Source Han Sans CN", Font.PLAIN, 18);
        g.setFont(bottomFont);
        String bottom = "家谱管理系统 技术驱动家族文化传承";
        int bottomWidth = g.getFontMetrics(bottomFont).stringWidth(bottom);
        g.setColor(Color.WHITE);
        g.drawString(bottom, (WIDTH - bottomWidth) / 2, 1250);

        g.dispose();

        // 输出PNG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] bytes = baos.toByteArray();

        log.info("生成分享海报成功: {}", genealogyName);
        return bytes;
    }
}
