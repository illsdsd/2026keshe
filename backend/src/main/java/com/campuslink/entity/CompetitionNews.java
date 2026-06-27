package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 赛事资讯/公告实体，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("competition_news")
public class CompetitionNews {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long competitionId;

    private String title;

    private String content;

    private Long authorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
