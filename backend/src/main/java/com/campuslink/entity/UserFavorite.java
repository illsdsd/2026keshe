package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏实体，v2 新增。
 *
 * <p>ref_type 取 COMPETITION / TEAM。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("user_favorite")
public class UserFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String refType;

    private Long refId;

    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
