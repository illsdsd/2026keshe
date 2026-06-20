package com.campuslink.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建队伍请求。
 */
@Data
public class TeamCreateDTO {

    @NotBlank(message = "队伍名称不能为空")
    private String name;

    private Long competitionId;

    private String intro;

    @NotNull(message = "请填写需要总人数")
    @Min(value = 1, message = "总人数至少为 1")
    private Integer totalSize;

    private String college;

    /** 招募岗位 */
    private List<RecruitItem> recruits;

    @Data
    public static class RecruitItem {
        @NotBlank(message = "岗位名称不能为空")
        private String position;

        @Min(value = 1, message = "岗位人数至少为 1")
        private Integer count = 1;
    }
}
