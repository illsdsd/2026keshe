package com.campuslink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储相关配置项。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "campuslink.file")
public class FileStorageProperties {

    private String storagePath;//本地存储根目录

    private Integer maxSizeMb;//单文件最大大小（MB）

    private String allowedExtensions;//允许扩展名（逗号分隔）
}
