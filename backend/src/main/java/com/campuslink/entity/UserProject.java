package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户作品集实体，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("user_project")
public class UserProject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String cover;

    private String intro;

    private String link;

    private String award;

    private String role;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
