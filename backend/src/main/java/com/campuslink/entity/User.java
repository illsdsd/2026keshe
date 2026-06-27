package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体。
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** 密码仅写入、不返回前端 */
    @JsonIgnore
    private String password;

    private String nickname;

    private String email;

    private String avatar;

    private String college;

    private String major;

    private Integer grade;

    private String intro;

    private BigDecimal reputation;

    /** STUDENT / ADMIN */
    private String role;

    /** 0禁用/1启用，v2 新增 */
    private Integer enabled;

    /** 手机号，v2 新增 */
    private String phone;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
}
