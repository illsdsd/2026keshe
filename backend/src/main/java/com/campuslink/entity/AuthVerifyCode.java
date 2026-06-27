package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮箱验证码实体，v2 新增（用于找回密码 / 更换邮箱场景）。
 *
 * <p>scene 取 RESET_PWD / CHANGE_EMAIL；mock 模式下验证码仅控制台打印。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("auth_verify_code")
public class AuthVerifyCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String email;

    private String scene;

    private String code;

    private LocalDateTime expireAt;

    private Integer used;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
