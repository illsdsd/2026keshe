package com.campuslink.schedule;

import com.campuslink.service.FileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 临时文件清理定时任务，v2 新增。
 *
 * <p>策略：每天凌晨 4 点扫描 expire_at 小于当前时间的文件，删除磁盘与元数据。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Component
@RequiredArgsConstructor
public class FileCleanScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCleanScheduler.class);

    private final FileService fileService;

    @Scheduled(cron = "${campuslink.schedule.file-clean-cron:0 0 4 * * ?}")
    public void clean() {
        LOGGER.info("[定时] 临时文件清理启动");
        int cleaned = fileService.cleanExpired();
        LOGGER.info("[定时] 临时文件清理结束, 已清理 {} 条", cleaned);
    }
}
