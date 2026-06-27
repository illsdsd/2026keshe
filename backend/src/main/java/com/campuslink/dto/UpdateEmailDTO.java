package com.campuslink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更换邮箱请求。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
public class UpdateEmailDTO {

    @NotBlank
    @Email
    private String newEmail;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
