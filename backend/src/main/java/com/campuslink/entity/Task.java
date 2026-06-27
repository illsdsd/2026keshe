package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务实体。
 */
@Data
@TableName("task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private String title;

    private String description;

    private Long assigneeId;

    private LocalDateTime deadline;

    /** TODO / DOING / DONE */
    private String status;

    /** LOW / MEDIUM / HIGH，v2 新增 */
    private String priority;

    /** 逗号分隔标签，v2 新增 */
    private String tags;

    /** 父任务 id，v2 子任务时填入主任务 id，主任务保持 null */
    private Long parentId;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
