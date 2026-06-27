package com.campuslink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 账号安全相关配置项。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "campuslink.security")
public class SecurityProperties {

    private Integer maxLoginFail = 5;//最大允许失败次数

    private Integer lockMinutes = 10;//锁定时长（分钟）
}
