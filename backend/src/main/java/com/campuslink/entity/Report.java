package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报实体，v2 新增。
 *
 * <p>target_type 取 EVALUATION / TEAM / USER / POST。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String targetType;

    private Long targetId;

    private Long reporterId;

    private String reason;

    private String status;

    private String handleRemark;

    private Long handlerId;

    private LocalDateTime handleTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
