package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.ForgetPasswordDTO;
import com.campuslink.dto.LoginDTO;
import com.campuslink.dto.LoginVO;
import com.campuslink.dto.RegisterDTO;
import com.campuslink.dto.ResetPasswordDTO;
import com.campuslink.service.AuthService;
import com.campuslink.service.AuthVerifyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口：注册、登录、退出、找回密码。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthVerifyService authVerifyService;

    /**
     * 用户注册。
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    /**
     * 用户登录。
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        return Result.success(authService.login(dto, request));
    }

    /**
     * 退出登录（无状态 JWT 仅作占位，便于前端统一调用）。
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    /**
     * 找回密码 - 发送验证码。
     */
    @PostMapping("/password/forget")
    public Result<Void> forget(@Valid @RequestBody ForgetPasswordDTO dto) {
        authVerifyService.sendCode(dto.getEmail(), "RESET_PWD");
        return Result.success();
    }

    /**
     * 找回密码 - 凭验证码重置。
     */
    @PostMapping("/password/reset")
    public Result<Void> reset(@Valid @RequestBody ResetPasswordDTO dto) {
        authVerifyService.resetPassword(dto.getEmail(), dto.getCode(), dto.getNewPassword());
        return Result.success();
    }
}
