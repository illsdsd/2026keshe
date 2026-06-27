package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.common.BusinessException;
import com.campuslink.common.ResultCode;
import com.campuslink.entity.AuthVerifyCode;
import com.campuslink.entity.User;
import com.campuslink.mapper.AuthVerifyCodeMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 邮箱验证码服务，v2 新增。
 *
 * <p>用于找回密码、更换邮箱等场景。验证码 6 位数字、5 分钟有效。
 * 当前模式下验证码通过 {@link MailService} 控制台打印，不发真实邮件。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class AuthVerifyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthVerifyService.class);
    private static final int CODE_TTL_MINUTES = 5;

    private final AuthVerifyCodeMapper codeMapper;
    private final UserMapper userMapper;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 发送验证码到指定邮箱。
     */
    public void sendCode(String email, String scene) {
        //1 邮箱是否绑定用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            LOGGER.warn("发送验证码失败：邮箱未注册, email={}", email);
            throw new BusinessException(ResultCode.NOT_FOUND, "该邮箱未绑定任何账号");
        }

        //2 生成 6 位验证码
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        AuthVerifyCode entity = new AuthVerifyCode();
        entity.setEmail(email);
        entity.setScene(scene);
        entity.setCode(code);
        entity.setExpireAt(LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES));
        entity.setUsed(0);
        codeMapper.insert(entity);
        LOGGER.info("验证码已生成, email={}, scene={}, code=***, expireAt={}", email, scene, entity.getExpireAt());

        //3 异步发送（mock 直接打印）
        mailService.sendVerifyCode(email, code, scene);
    }

    /**
     * 校验验证码并标记为已用。
     */
    public void verify(String email, String code, String scene) {
        AuthVerifyCode record = codeMapper.selectOne(new LambdaQueryWrapper<AuthVerifyCode>()
                .eq(AuthVerifyCode::getEmail, email)
                .eq(AuthVerifyCode::getScene, scene)
                .eq(AuthVerifyCode::getCode, code)
                .eq(AuthVerifyCode::getUsed, 0)
                .orderByDesc(AuthVerifyCode::getCreateTime)
                .last("LIMIT 1"));

        if (record == null) {
            LOGGER.warn("验证码校验失败：无匹配记录, email={}, scene={}", email, scene);
            throw new BusinessException(ResultCode.VERIFY_CODE_INVALID);
        }
        if (record.getExpireAt().isBefore(LocalDateTime.now())) {
            LOGGER.warn("验证码校验失败：已过期, email={}, recordId={}", email, record.getId());
            throw new BusinessException(ResultCode.VERIFY_CODE_INVALID, "验证码已过期，请重新获取");
        }

        record.setUsed(1);
        codeMapper.updateById(record);
        LOGGER.info("验证码校验通过, email={}, scene={}", email, scene);
    }

    /**
     * 重置密码（校验码 + 写新密码）。
     */
    public void resetPassword(String email, String code, String newPassword) {
        verify(email, code, "RESET_PWD");

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "邮箱对应账号不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        LOGGER.info("密码已重置, userId={}", user.getId());
    }
}
