package com.campuslink.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 队伍成员视图。
 */
@Data
public class TeamMemberVO {

    private Long userId;

    private String nickname;

    private String avatar;

    private String major;

    private Integer grade;

    /** LEADER / MEMBER */
    private String role;

    private LocalDateTime joinTime;
}
