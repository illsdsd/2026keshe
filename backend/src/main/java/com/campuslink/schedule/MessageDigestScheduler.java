package com.campuslink.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.entity.Message;
import com.campuslink.mapper.MessageMapper;
import com.campuslink.service.MailService;
import com.campuslink.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每日未读消息摘要定时任务，v2 新增。
 *
 * <p>策略：每天 20 点扫描所有未读消息，按 receiver 聚合数量，给每个有未读
 * 消息的用户发一条 SYSTEM 类型站内消息 + mock 邮件提醒。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Component
@RequiredArgsConstructor
public class MessageDigestScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDigestScheduler.class);

    private final MessageMapper messageMapper;
    private final MessageService messageService;
    private final MailService mailService;

    @Scheduled(cron = "${campuslink.schedule.message-digest-cron:0 0 20 * * ?}")
    public void digest() {
        LocalDateTime since = LocalDateTime.now().minusDays(1);//近 24 小时
        LOGGER.info("[定时] 未读消息摘要启动, since={}", since);

        List<Message> unread = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getIsRead, 0)
                .ge(Message::getCreateTime, since));
        if (unread.isEmpty()) {
            LOGGER.info("[定时] 当前没有未读消息需要汇总");
            return;
        }

        Map<Long, Integer> counter = new HashMap<>();
        for (Message msg : unread) {
            counter.merge(msg.getReceiverId(), 1, Integer::sum);
        }

        for (Map.Entry<Long, Integer> entry : counter.entrySet()) {
            Long userId = entry.getKey();
            Integer count = entry.getValue();
            try {
                messageService.create(userId, "DIGEST",
                        "您今日有 " + count + " 条未读消息，请前往「消息中心」查看",
                        null);
                mailService.sendPlainText("user-" + userId + "@campuslink.local",
                        "CampusLink 每日消息摘要",
                        "您今日共有 " + count + " 条未读消息");
                LOGGER.info("[定时] 摘要消息已发送, userId={}, count={}", userId, count);
            } catch (Exception e) {
                LOGGER.warn("[定时] 摘要消息发送失败, userId={}", userId, e);
            }
        }
        LOGGER.info("[定时] 未读消息摘要结束, 用户数={}", counter.size());
    }
}
