package com.campuslink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 找回密码 - 发送验证码请求。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
public class ForgetPasswordDTO {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
