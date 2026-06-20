package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 招募岗位实体。
 */
@Data
@TableName("team_recruit")
public class TeamRecruit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private String position;

    private Integer count;

    private Integer filled;
}
