package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户隐私与推送设置实体，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("user_privacy")
public class UserPrivacy {

    @TableId
    private Long userId;

    private Integer profilePublic;

    private Integer reputationPublic;

    private Integer pushApply;

    private Integer pushAudit;

    private Integer pushTask;

    private Integer pushNotice;

    private Integer pushEval;

    private LocalDateTime updateTime;
}
