package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 赛事报名实体，v2 新增。
 *
 * <p>status 取 PENDING / APPROVED / REJECTED。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("competition_register")
public class CompetitionRegister {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long competitionId;

    private Long teamId;

    private Long applicantId;

    private String remark;

    private String status;

    private String auditReason;

    private LocalDateTime auditTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
