package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 队伍动态实体，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("team_post")
public class TeamPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private Long authorId;

    private String content;

    /** 图片 URL，多张用逗号分隔 */
    private String imageUrls;

    private Integer likeCount;

    private Integer commentCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
