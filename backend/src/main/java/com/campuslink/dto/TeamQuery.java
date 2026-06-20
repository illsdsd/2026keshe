package com.campuslink.dto;

import lombok.Data;

/**
 * 队伍多维筛选参数。
 */
@Data
public class TeamQuery {

    private Integer current = 1;

    private Integer size = 10;

    /** 关联竞赛 */
    private Long competitionId;

    /** 学院 */
    private String college;

    /** 队长年级 */
    private Integer grade;

    /** 技能 / 招募岗位关键词（匹配招募岗位名称） */
    private String skill;

    /** 状态 RECRUITING / FULL / CLOSED */
    private String status;

    /** 队伍名称 / 简介关键词 */
    private String keyword;
}
