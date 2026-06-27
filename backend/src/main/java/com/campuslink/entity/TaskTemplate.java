package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务模板实体，v2 新增。
 *
 * <p>payload 字段以 JSON 字符串存储任务结构（包含若干 title/priority/tags/子任务），
 * 反序列化后用于一键复用建任务。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("task_template")
public class TaskTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ownerId;

    private String name;

    private String payload;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
