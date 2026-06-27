package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 赛事附件实体，v2 新增。
 *
 * <p>category 取 NOTICE / RULE / FORM / OTHER。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("competition_attachment")
public class CompetitionAttachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long competitionId;

    private Long fileId;

    private String name;

    private String category;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
