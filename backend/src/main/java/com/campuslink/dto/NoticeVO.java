package com.campuslink.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告视图：公告信息 + 发布者昵称。
 */
@Data
public class NoticeVO {

    private Long id;

    private Long teamId;

    private Long authorId;

    private String authorName;

    private String title;

    private String content;

    private LocalDateTime createTime;
}
