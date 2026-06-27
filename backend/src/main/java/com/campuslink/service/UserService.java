package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.BusinessException;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.UpdateEmailDTO;
import com.campuslink.dto.UpdatePasswordDTO;
import com.campuslink.dto.UserUpdateDTO;
import com.campuslink.entity.Skill;
import com.campuslink.entity.User;
import com.campuslink.entity.UserCertificate;
import com.campuslink.entity.UserFavorite;
import com.campuslink.entity.UserLoginLog;
import com.campuslink.entity.UserPrivacy;
import com.campuslink.entity.UserProject;
import com.campuslink.entity.UserSkill;
import com.campuslink.mapper.SkillMapper;
import com.campuslink.mapper.UserCertificateMapper;
import com.campuslink.mapper.UserFavoriteMapper;
import com.campuslink.mapper.UserLoginLogMapper;
import com.campuslink.mapper.UserMapper;
import com.campuslink.mapper.UserPrivacyMapper;
import com.campuslink.mapper.UserProjectMapper;
import com.campuslink.mapper.UserSkillMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户信息与扩展功能服务。
 *
 * <p>v2 在 v1 基础上新增：修改密码 / 更换邮箱 / 隐私设置 / 登录日志 / 收藏 /
 * 作品集 / 证书等子能力。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserMapper userMapper;
    private final SkillMapper skillMapper;
    private final UserSkillMapper userSkillMapper;
    private final UserPrivacyMapper privacyMapper;
    private final UserLoginLogMapper loginLogMapper;
    private final UserFavoriteMapper favoriteMapper;
    private final UserProjectMapper projectMapper;
    private final UserCertificateMapper certificateMapper;
    private final AuthVerifyService authVerifyService;
    private final PasswordEncoder passwordEncoder;

    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    public User updateProfile(Long userId, UserUpdateDTO dto) {
        User user = getById(userId);
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getCollege() != null) user.setCollege(dto.getCollege());
        if (dto.getMajor() != null) user.setMajor(dto.getMajor());
        if (dto.getGrade() != null) user.setGrade(dto.getGrade());
        if (dto.getIntro() != null) user.setIntro(dto.getIntro());
        userMapper.updateById(user);
        LOGGER.info("用户资料已更新, userId={}", userId);
        return user;
    }

    public List<Skill> listUserSkills(Long userId) {
        return userSkillMapper.selectSkillsByUserId(userId);
    }

    public void addSkill(Long userId, Long skillId) {
        if (skillMapper.selectById(skillId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "技能不存在");
        }
        Long exists = userSkillMapper.selectCount(new LambdaQueryWrapper<UserSkill>()
                .eq(UserSkill::getUserId, userId)
                .eq(UserSkill::getSkillId, skillId));
        if (exists != null && exists > 0) {
            return;
        }
        UserSkill us = new UserSkill();
        us.setUserId(userId);
        us.setSkillId(skillId);
        userSkillMapper.insert(us);
    }

    public void removeSkill(Long userId, Long skillId) {
        userSkillMapper.delete(new LambdaQueryWrapper<UserSkill>()
                .eq(UserSkill::getUserId, userId)
                .eq(UserSkill::getSkillId, skillId));
    }

    /**
     * 修改密码（校验原密码）。
     */
    public void updatePassword(Long userId, UpdatePasswordDTO dto) {
        User user = getById(userId);
        boolean ok = passwordEncoder.matches(dto.getOldPassword(), user.getPassword());//原密码校验
        if (ok) {//校验通过更新
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            userMapper.updateById(user);
            LOGGER.info("密码已修改, userId={}", userId);
        } else {//原密码错
            LOGGER.warn("修改密码失败：原密码错误, userId={}", userId);
            throw new BusinessException(ResultCode.PASSWORD_NOT_MATCH, "原密码错误");
        }
    }

    /**
     * 更换邮箱（需校验旧邮箱验证码）。
     */
    public void updateEmail(Long userId, UpdateEmailDTO dto) {
        User user = getById(userId);
        String currentEmail = user.getEmail();//当前邮箱
        if (currentEmail == null || currentEmail.isBlank()) {//从未绑定邮箱
            LOGGER.info("当前账号未绑定邮箱，直接绑定新邮箱, userId={}", userId);
        } else {//走验证码校验
            authVerifyService.verify(currentEmail, dto.getCode(), "CHANGE_EMAIL");
        }

        user.setEmail(dto.getNewEmail());
        userMapper.updateById(user);
        LOGGER.info("邮箱已更新, userId={}, newEmail={}", userId, dto.getNewEmail());
    }

    /**
     * 获取或初始化隐私设置。
     */
    public UserPrivacy getPrivacy(Long userId) {
        UserPrivacy privacy = privacyMapper.selectById(userId);
        if (privacy == null) {
            privacy = new UserPrivacy();
            privacy.setUserId(userId);
            privacy.setProfilePublic(1);
            privacy.setReputationPublic(1);
            privacy.setPushApply(1);
            privacy.setPushAudit(1);
            privacy.setPushTask(1);
            privacy.setPushNotice(1);
            privacy.setPushEval(1);
            privacy.setUpdateTime(LocalDateTime.now());
            privacyMapper.insert(privacy);
            LOGGER.info("隐私设置初始化, userId={}", userId);
        }
        return privacy;
    }

    /**
     * 更新隐私设置。
     */
    public UserPrivacy updatePrivacy(Long userId, UserPrivacy patch) {
        UserPrivacy current = getPrivacy(userId);
        if (patch.getProfilePublic() != null) current.setProfilePublic(patch.getProfilePublic());
        if (patch.getReputationPublic() != null) current.setReputationPublic(patch.getReputationPublic());
        if (patch.getPushApply() != null) current.setPushApply(patch.getPushApply());
        if (patch.getPushAudit() != null) current.setPushAudit(patch.getPushAudit());
        if (patch.getPushTask() != null) current.setPushTask(patch.getPushTask());
        if (patch.getPushNotice() != null) current.setPushNotice(patch.getPushNotice());
        if (patch.getPushEval() != null) current.setPushEval(patch.getPushEval());
        current.setUpdateTime(LocalDateTime.now());
        privacyMapper.updateById(current);
        LOGGER.info("隐私设置已更新, userId={}", userId);
        return current;
    }

    /**
     * 我的登录日志（分页）。
     */
    public Map<String, Object> pageLoginLogs(Long userId, long current, long size) {
        Page<UserLoginLog> page = loginLogMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<UserLoginLog>()
                        .eq(UserLoginLog::getUserId, userId)
                        .orderByDesc(UserLoginLog::getLoginTime));
        return PageResult.wrap(page);
    }

    /**
     * 项目作品集相关。
     */
    public List<UserProject> listProjects(Long userId) {
        return projectMapper.selectList(new LambdaQueryWrapper<UserProject>()
                .eq(UserProject::getUserId, userId)
                .orderByDesc(UserProject::getCreateTime));
    }

    public UserProject saveProject(Long userId, UserProject project) {
        project.setUserId(userId);
        if (project.getId() == null) {
            projectMapper.insert(project);
            LOGGER.info("作品已新增, userId={}, projectId={}", userId, project.getId());
        } else {
            UserProject existing = projectMapper.selectById(project.getId());
            if (existing == null || !existing.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无法修改他人作品");
            }
            projectMapper.updateById(project);
            LOGGER.info("作品已更新, projectId={}", project.getId());
        }
        return project;
    }

    public void deleteProject(Long userId, Long projectId) {
        UserProject existing = projectMapper.selectById(projectId);
        if (existing == null) {
            return;
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无法删除他人作品");
        }
        projectMapper.deleteById(projectId);
        LOGGER.info("作品已删除, projectId={}", projectId);
    }

    public List<UserCertificate> listCertificates(Long userId) {
        return certificateMapper.selectList(new LambdaQueryWrapper<UserCertificate>()
                .eq(UserCertificate::getUserId, userId)
                .orderByDesc(UserCertificate::getCreateTime));
    }

    public UserCertificate addCertificate(Long userId, UserCertificate certificate) {
        certificate.setUserId(userId);
        certificateMapper.insert(certificate);
        LOGGER.info("证书已上传, userId={}, certId={}", userId, certificate.getId());
        return certificate;
    }

    public void deleteCertificate(Long userId, Long certId) {
        UserCertificate cert = certificateMapper.selectById(certId);
        if (cert == null) {
            return;
        }
        if (!cert.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        certificateMapper.deleteById(certId);
    }

    /**
     * 收藏。
     */
    public UserFavorite addFavorite(Long userId, String refType, Long refId, String note) {
        Long exists = favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getRefType, refType)
                .eq(UserFavorite::getRefId, refId));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_RECORD, "已经收藏过了");
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setRefType(refType);
        favorite.setRefId(refId);
        favorite.setNote(note);
        favoriteMapper.insert(favorite);
        LOGGER.info("收藏已添加, userId={}, refType={}, refId={}", userId, refType, refId);
        return favorite;
    }

    public void removeFavorite(Long userId, Long favoriteId) {
        UserFavorite existing = favoriteMapper.selectById(favoriteId);
        if (existing == null) {
            return;
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        favoriteMapper.deleteById(favoriteId);
        LOGGER.info("收藏已取消, userId={}, favoriteId={}", userId, favoriteId);
    }

    public List<UserFavorite> listFavorites(Long userId, String refType) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getCreateTime);
        if (refType != null && !refType.isBlank()) {
            wrapper.eq(UserFavorite::getRefType, refType);
        }
        return favoriteMapper.selectList(wrapper);
    }
}
