package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String method;

    private String path;

    private String params;

    private String ip;

    private String status;

    private String errorMsg;

    private Integer costMs;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
