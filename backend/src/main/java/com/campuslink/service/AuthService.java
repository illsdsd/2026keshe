package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.common.BusinessException;
import com.campuslink.common.ResultCode;
import com.campuslink.config.SecurityProperties;
import com.campuslink.dto.LoginDTO;
import com.campuslink.dto.LoginVO;
import com.campuslink.dto.RegisterDTO;
import com.campuslink.entity.User;
import com.campuslink.entity.UserLoginLog;
import com.campuslink.entity.UserPrivacy;
import com.campuslink.mapper.UserLoginLogMapper;
import com.campuslink.mapper.UserMapper;
import com.campuslink.mapper.UserPrivacyMapper;
import com.campuslink.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 注册 / 登录服务，v2 升级支持账号锁定与登录日志。
 *
 * <p>账号锁定策略：最近 {@code lockMinutes} 分钟内同一账号失败次数 ≥
 * {@code maxLoginFail} 则锁定，本次直接抛 {@link ResultCode#ACCOUNT_LOCKED}。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserLoginLogMapper loginLogMapper;
    private final UserPrivacyMapper privacyMapper;
    private final SecurityProperties securityProperties;

    /**
     * 注册新用户。
     */
    public void register(RegisterDTO dto) {
        //1 判重
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            LOGGER.warn("注册失败，账号已存在, username={}", dto.getUsername());
            throw new BusinessException("账号已存在");
        }

        //2 写入 user 表，默认 STUDENT、reputation=5.00
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setCollege(dto.getCollege());
        user.setMajor(dto.getMajor());
        user.setGrade(dto.getGrade());
        user.setReputation(new BigDecimal("5.00"));
        user.setRole("STUDENT");
        user.setEnabled(1);
        userMapper.insert(user);
        LOGGER.info("注册成功, userId={}, username={}", user.getId(), user.getUsername());

        //3 初始化隐私设置
        UserPrivacy privacy = new UserPrivacy();
        privacy.setUserId(user.getId());
        privacy.setProfilePublic(1);
        privacy.setReputationPublic(1);
        privacy.setPushApply(1);
        privacy.setPushAudit(1);
        privacy.setPushTask(1);
        privacy.setPushNotice(1);
        privacy.setPushEval(1);
        privacy.setUpdateTime(LocalDateTime.now());
        privacyMapper.insert(privacy);
    }

    /**
     * 登录（兼容 v1，无 request 上下文版本）。
     */
    public LoginVO login(LoginDTO dto) {
        return login(dto, null);
    }

    /**
     * 登录主流程（v2 含锁定 + 日志）。
     */
    public LoginVO login(LoginDTO dto, HttpServletRequest request) {
        String username = dto.getUsername();//账号
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        //1 账号不存在
        if (user == null) {
            LOGGER.warn("登录失败：账号不存在, username={}", username);
            recordLoginLog(null, username, request, false, "账号不存在");
            throw new BusinessException("账号或密码错误");
        }

        //2 账号禁用
        if (user.getEnabled() != null && user.getEnabled() == 0) {
            LOGGER.warn("登录失败：账号已禁用, userId={}", user.getId());
            recordLoginLog(user.getId(), username, request, false, "账号已禁用");
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被管理员禁用");
        }

        //3 锁定判定
        boolean locked = isLocked(username);
        if (locked) {
            LOGGER.warn("登录失败：账号已锁定, userId={}, username={}", user.getId(), username);
            recordLoginLog(user.getId(), username, request, false, "账号锁定中");
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        } else {
            //4 校验密码并写日志
            boolean passwordOk = passwordEncoder.matches(dto.getPassword(), user.getPassword());//密码是否匹配
            recordLoginLog(user.getId(), username, request, passwordOk, passwordOk ? null : "密码错误");
            if (passwordOk) {//成功签发 token
                String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
                LOGGER.info("登录成功, userId={}", user.getId());
                return new LoginVO(token, user);
            } else {//密码错
                LOGGER.warn("登录失败：密码错误, userId={}", user.getId());
                throw new BusinessException("账号或密码错误");
            }
        }
    }

    /**
     * 判断账号是否锁定（最近 N 分钟失败次数 ≥ 阈值）。
     */
    private boolean isLocked(String username) {
        int lockMinutes = securityProperties.getLockMinutes() == null ? 10 : securityProperties.getLockMinutes();
        int maxFail = securityProperties.getMaxLoginFail() == null ? 5 : securityProperties.getMaxLoginFail();
        LocalDateTime since = LocalDateTime.now().minusMinutes(lockMinutes);

        Long failCount = loginLogMapper.selectCount(new LambdaQueryWrapper<UserLoginLog>()
                .eq(UserLoginLog::getUsername, username)
                .eq(UserLoginLog::getSuccess, 0)
                .ge(UserLoginLog::getLoginTime, since));
        return failCount != null && failCount >= maxFail;
    }

    /**
     * 写登录日志。
     */
    private void recordLoginLog(Long userId, String username, HttpServletRequest request,
                                boolean success, String failReason) {
        UserLoginLog log = new UserLoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        if (request != null) {
            log.setIp(request.getRemoteAddr());
            log.setDevice(request.getHeader("User-Agent"));
        }
        log.setSuccess(success ? 1 : 0);
        log.setFailReason(failReason);
        try {
            loginLogMapper.insert(log);
        } catch (Exception e) {
            LOGGER.warn("写登录日志失败, username={}", username, e);
        }
    }
}
