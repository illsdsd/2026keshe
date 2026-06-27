package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 队伍成员实体。
 */
@Data
@TableName("team_member")
public class TeamMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private Long userId;

    /** LEADER / LEADER_DEPUTY / MEMBER（v2 新增 LEADER_DEPUTY） */
    private String role;

    /** 任命副队长时间，v2 新增 */
    private LocalDateTime deputyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinTime;
}
