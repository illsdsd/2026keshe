package com.campuslink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件配置项，mock=true 时验证码仅控制台打印，便于演示与单元测试。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "campuslink.mail")
public class MailProperties {

    private Boolean mock = Boolean.TRUE;//是否模拟邮件（不调真实 SMTP）

    private String sender;//发件人
}
