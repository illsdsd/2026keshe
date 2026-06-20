package com.campuslink.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 队伍列表项（含竞赛名、队长名、缺口岗位拼接）。
 */
@Data
public class TeamListVO {

    private Long id;

    private String name;

    private Long competitionId;

    private String competitionName;

    private Long leaderId;

    private String leaderName;

    private String intro;

    private Integer totalSize;

    private Integer currentSize;

    private String college;

    private String status;

    private LocalDateTime createTime;

    /** 缺口岗位拼接，如 "Vue 开发×1, UI 设计×1" */
    private String vacancies;
}
