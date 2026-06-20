package com.campuslink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发布公告请求。
 */
@Data
public class NoticeDTO {

    @NotNull(message = "队伍 id 不能为空")
    private Long teamId;

    @NotBlank(message = "公告标题不能为空")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;
}
