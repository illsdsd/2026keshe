package com.campuslink.dto;

import com.campuslink.entity.Team;
import com.campuslink.entity.TeamRecruit;
import lombok.Data;

import java.util.List;

/**
 * 队伍详情视图：队伍基本信息 + 竞赛名 + 队长名 + 招募岗位 + 成员。
 */
@Data
public class TeamDetailVO {

    private Team team;

    private String competitionName;

    private String leaderName;

    private List<TeamRecruit> recruits;

    private List<TeamMemberVO> members;
}
