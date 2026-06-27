package com.campuslink.service;

import com.campuslink.config.MailProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 邮件服务，v2 新增。
 *
 * <p>当前 mock 模式下仅在控制台打印验证码与正文，便于课设演示，不接入真实 SMTP。
 * 后续切换到生产时只需把 {@code campuslink.mail.mock=false} 并补充 Spring Mail 配置即可。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private final MailProperties mailProperties;

    /**
     * 发送验证码邮件。
     *
     * @param email 接收邮箱
     * @param code  6 位验证码
     * @param scene 业务场景描述（用于日志/邮件正文）
     */
    @Async
    public void sendVerifyCode(String email, String code, String scene) {
        boolean mockMode = Boolean.TRUE.equals(mailProperties.getMock());//是否 mock
        if (mockMode) {//mock 直接控制台打印
            LOGGER.info("【MOCK_MAIL】to={}, scene={}, sender={}, body=您的验证码为 {}（5 分钟内有效）",
                    email, scene, mailProperties.getSender(), code);
        } else {//真实 SMTP 暂未实现，留扩展位
            LOGGER.warn("当前邮件 mock=false 但 SMTP 未配置，无法发送邮件, email={}, scene={}", email, scene);
            throw new IllegalStateException("生产邮件发送尚未配置 SMTP");
        }
    }

    /**
     * 发送通用文本邮件（如评分通知）。
     */
    @Async
    public void sendPlainText(String email, String subject, String body) {
        boolean mockMode = Boolean.TRUE.equals(mailProperties.getMock());
        if (mockMode) {
            LOGGER.info("【MOCK_MAIL】to={}, subject={}, body={}", email, subject, body);
        } else {
            LOGGER.warn("当前邮件 mock=false 但 SMTP 未配置，无法发送邮件, email={}, subject={}", email, subject);
        }
    }
}
