package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.entity.Notice;
import com.campuslink.security.SecurityUtil;
import com.campuslink.service.MessageExtraService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息中心 / 公告拓展接口，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@RestController
@RequiredArgsConstructor
public class MessageExtraController {

    private final MessageExtraService messageExtraService;

    @GetMapping("/message")
    public Result<Map<String, Object>> list(@RequestParam(required = false) String type,
                                            @RequestParam(required = false) Integer isRead,
                                            @RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size) {
        return Result.success(messageExtraService.listMessages(SecurityUtil.getUserId(), type, isRead, current, size));
    }

    @GetMapping("/message/unread/count")
    public Result<Map<String, Object>> unread() {
        long count = messageExtraService.unreadCount(SecurityUtil.getUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    @PutMapping("/message/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        messageExtraService.markRead(SecurityUtil.getUserId(), id);
        return Result.success();
    }

    @PutMapping("/message/read-all")
    public Result<Void> readAll() {
        messageExtraService.markAllRead(SecurityUtil.getUserId());
        return Result.success();
    }

    @PutMapping("/message/batch-read")
    public Result<Void> batchRead(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Object> rawIds = (List<Object>) body.getOrDefault("ids", List.of());
        List<Long> ids = rawIds.stream().map(o -> Long.valueOf(o.toString())).toList();
        messageExtraService.batchRead(SecurityUtil.getUserId(), ids);
        return Result.success();
    }

    @DeleteMapping("/message/batch")
    public Result<Void> batchDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Object> rawIds = (List<Object>) body.getOrDefault("ids", List.of());
        List<Long> ids = rawIds.stream().map(o -> Long.valueOf(o.toString())).toList();
        messageExtraService.batchDelete(SecurityUtil.getUserId(), ids);
        return Result.success();
    }

    @GetMapping("/message/search")
    public Result<Map<String, Object>> search(@RequestParam String keyword,
                                              @RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "10") long size) {
        return Result.success(messageExtraService.search(SecurityUtil.getUserId(), keyword, current, size));
    }

    @PostMapping("/notice/schedule")
    public Result<Notice> scheduleNotice(@RequestBody Map<String, Object> body) {
        Long teamId = Long.valueOf(body.get("teamId").toString());
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        LocalDateTime publishAt = LocalDateTime.parse((String) body.get("publishAt"));
        return Result.success(messageExtraService.scheduleNotice(teamId, SecurityUtil.getUserId(), title, content, publishAt));
    }
}
