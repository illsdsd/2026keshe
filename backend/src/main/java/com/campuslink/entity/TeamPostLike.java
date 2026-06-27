package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 队伍动态点赞实体，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("team_post_like")
public class TeamPostLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
