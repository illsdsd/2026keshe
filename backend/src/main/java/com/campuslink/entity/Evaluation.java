package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 互评实体（v1 表已存在但缺少 Entity 映射，v2 补全）。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("evaluation")
public class Evaluation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private Long fromUserId;

    private Long toUserId;

    private Integer responsibility;

    private Integer tech;

    private Integer communication;

    /** 是否匿名（v2 扩展，原表无此列，仅在 v2 增量脚本里追加；为简单不破坏 v1 SQL，本字段属于 v2 扩展，暂用 0 默认 ） */
    @TableField(exist = false)
    private Integer anonymous;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
