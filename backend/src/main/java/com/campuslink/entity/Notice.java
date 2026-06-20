package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 队伍公告实体。
 */
@Data
@TableName("notice")
public class Notice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private Long authorId;

    private String title;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
