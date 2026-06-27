package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.entity.Message;
import com.campuslink.entity.Notice;
import com.campuslink.mapper.MessageMapper;
import com.campuslink.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息中心拓展业务（分类筛选 / 批量已读 / 批量删除 / 关键词检索 / 定时公告），v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class MessageExtraService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageExtraService.class);

    private final MessageMapper messageMapper;
    private final NoticeMapper noticeMapper;

    public Map<String, Object> listMessages(Long userId, String type, Integer isRead, long current, long size) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .orderByDesc(Message::getCreateTime);
        if (type != null && !type.isBlank()) {
            wrapper.eq(Message::getType, type);
        }
        if (isRead != null) {
            wrapper.eq(Message::getIsRead, isRead);
        }
        Page<Message> page = messageMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.wrap(page);
    }

    public long unreadCount(Long userId) {
        Long cnt = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0));
        return cnt == null ? 0 : cnt;
    }

    public int markRead(Long userId, Long id) {
        return messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getId, id)
                .eq(Message::getReceiverId, userId)
                .set(Message::getIsRead, 1));
    }

    public int markAllRead(Long userId) {
        int n = messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1));
        LOGGER.info("全部已读, userId={}, 更新条数={}", userId, n);
        return n;
    }

    public int batchRead(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .in(Message::getId, ids)
                .set(Message::getIsRead, 1));
    }

    public int batchDelete(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return messageMapper.delete(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .in(Message::getId, ids));
    }

    public Map<String, Object> search(Long userId, String keyword, long current, long size) {
        Page<Message> page = messageMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, userId)
                        .like(keyword != null && !keyword.isBlank(), Message::getContent, keyword)
                        .orderByDesc(Message::getCreateTime));
        return PageResult.wrap(page);
    }

    /** 定时公告 */
    public Notice scheduleNotice(Long teamId, Long authorId, String title, String content, LocalDateTime publishAt) {
        Notice notice = new Notice();
        notice.setTeamId(teamId);
        notice.setAuthorId(authorId);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setPublishAt(publishAt);
        notice.setScheduled(1);
        noticeMapper.insert(notice);
        LOGGER.info("定时公告已创建, noticeId={}, publishAt={}", notice.getId(), publishAt);
        return notice;
    }
}
