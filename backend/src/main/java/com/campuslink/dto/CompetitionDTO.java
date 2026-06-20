package com.campuslink.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞赛新增 / 编辑请求。
 */
@Data
public class CompetitionDTO {

    @NotBlank(message = "竞赛名称不能为空")
    private String name;

    @NotBlank(message = "竞赛类型不能为空")
    private String type;

    private String intro;

    private LocalDateTime deadline;
}
