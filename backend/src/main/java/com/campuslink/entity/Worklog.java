package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工时填报实体，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("worklog")
public class Worklog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private Long taskId;

    private Long userId;

    private LocalDate workDate;

    private BigDecimal hours;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
