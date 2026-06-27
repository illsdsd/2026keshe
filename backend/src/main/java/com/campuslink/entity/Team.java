package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 队伍实体。
 */
@Data
@TableName("team")
public class Team {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long competitionId;

    private Long leaderId;

    private String intro;

    private Integer totalSize;

    private Integer currentSize;

    private String college;

    /** RECRUITING / FULL / CLOSED / ARCHIVED（v2 新增 ARCHIVED） */
    private String status;

    /** 归档时间，v2 新增 */
    private LocalDateTime archivedTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
