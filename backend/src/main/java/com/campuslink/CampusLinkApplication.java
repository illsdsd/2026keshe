package com.campuslink;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CampusLink 校园竞赛组队与协作平台 启动类。
 *
 * <p>v2 起开启 {@link EnableScheduling} 用于定时任务（招募过期、归档、逾期任务推送等），
 * 开启 {@link EnableAsync} 用于异步发送站内消息、邮件模拟。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@SpringBootApplication
@MapperScan("com.campuslink.mapper")
@EnableScheduling
@EnableAsync
public class CampusLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusLinkApplication.class, args);
    }
}
