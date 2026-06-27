package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.BusinessException;
import com.campuslink.common.ExcelExportUtil;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.entity.Apply;
import com.campuslink.entity.Competition;
import com.campuslink.entity.Report;
import com.campuslink.entity.Skill;
import com.campuslink.entity.SystemNotice;
import com.campuslink.entity.Task;
import com.campuslink.entity.Team;
import com.campuslink.entity.User;
import com.campuslink.mapper.ApplyMapper;
import com.campuslink.mapper.CompetitionMapper;
import com.campuslink.mapper.ReportMapper;
import com.campuslink.mapper.SkillMapper;
import com.campuslink.mapper.SystemNoticeMapper;
import com.campuslink.mapper.TaskMapper;
import com.campuslink.mapper.TeamMapper;
import com.campuslink.mapper.UserMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员后台业务，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminService.class);

    private final UserMapper userMapper;
    private final TeamMapper teamMapper;
    private final TaskMapper taskMapper;
    private final ApplyMapper applyMapper;
    private final CompetitionMapper competitionMapper;
    private final ReportMapper reportMapper;
    private final SkillMapper skillMapper;
    private final SystemNoticeMapper systemNoticeMapper;
    private final PasswordEncoder passwordEncoder;

    /** 用户列表 */
    public Map<String, Object> pageUsers(String keyword, String role, long current, long size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                    .or().like(User::getUsername, keyword)
                    .or().like(User::getEmail, keyword));
        }
        if (role != null && !role.isBlank()) {
            wrapper.eq(User::getRole, role);
        }
        Page<User> page = userMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.wrap(page);
    }

    public void disableUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        user.setEnabled(0);
        userMapper.updateById(user);
        LOGGER.warn("[ADMIN] 用户已禁用, userId={}", userId);
    }

    public void enableUser(Long userId) {
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getEnabled, 1));
        LOGGER.info("[ADMIN] 用户已启用, userId={}", userId);
    }

    public void resetPassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        LOGGER.warn("[ADMIN] 用户密码已被管理员重置, userId={}", userId);
    }

    public void exportUsers(HttpServletResponse response) throws IOException {
        List<User> users = userMapper.selectList(null);
        List<List<Object>> rows = new ArrayList<>();
        for (User u : users) {
            List<Object> row = new ArrayList<>();
            row.add(u.getId());
            row.add(u.getUsername());
            row.add(u.getNickname());
            row.add(u.getEmail());
            row.add(u.getCollege());
            row.add(u.getMajor());
            row.add(u.getGrade());
            row.add(u.getRole());
            row.add(u.getEnabled());
            row.add(u.getReputation());
            row.add(u.getCreateTime() == null ? "" : u.getCreateTime().toString());
            rows.add(row);
        }
        ExcelExportUtil.writeXlsx(response, "users-export",
                List.of("ID", "账号", "昵称", "邮箱", "学院", "专业", "年级", "角色", "启用", "信誉分", "注册时间"),
                rows);
        LOGGER.info("[ADMIN] 用户列表已导出, 数量={}", users.size());
    }

    /** 队伍 */
    public Map<String, Object> pageTeams(String keyword, String status, long current, long size) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<Team>().orderByDesc(Team::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Team::getName, keyword);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(Team::getStatus, status);
        }
        Page<Team> page = teamMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.wrap(page);
    }

    public void disbandTeam(Long teamId) {
        teamMapper.deleteById(teamId);
        LOGGER.warn("[ADMIN] 队伍已强制解散, teamId={}", teamId);
    }

    /** 举报 */
    public Map<String, Object> pageReports(String status, long current, long size) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<Report>().orderByDesc(Report::getCreateTime);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Report::getStatus, status);
        }
        Page<Report> page = reportMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.wrap(page);
    }

    public Report handleReport(Long reportId, boolean handled, String remark, Long operatorId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        report.setStatus(handled ? "HANDLED" : "REJECTED");
        report.setHandleRemark(remark);
        report.setHandlerId(operatorId);
        report.setHandleTime(LocalDateTime.now());
        reportMapper.updateById(report);
        LOGGER.info("[ADMIN] 举报已处理, reportId={}, handled={}", reportId, handled);
        return report;
    }

    /** 数据大盘 */
    public Map<String, Object> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("teamCount", teamMapper.selectCount(null));
        data.put("competitionCount", competitionMapper.selectCount(null));
        data.put("taskCount", taskMapper.selectCount(null));
        data.put("applyCount", applyMapper.selectCount(null));
        data.put("reportPending", reportMapper.selectCount(new LambdaQueryWrapper<Report>().eq(Report::getStatus, "PENDING")));

        // 近 7 天注册趋势
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime next = day.plusDays(1);
            Long cnt = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .ge(User::getCreateTime, day)
                    .lt(User::getCreateTime, next));
            Map<String, Object> point = new HashMap<>();
            point.put("date", day.toLocalDate().toString());
            point.put("count", cnt == null ? 0 : cnt);
            trend.add(point);
        }
        data.put("registerTrend", trend);
        return data;
    }

    /** 系统公告 */
    public SystemNotice publishSystemNotice(String title, String content, Long publisherId) {
        SystemNotice notice = new SystemNotice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setPublisherId(publisherId);
        notice.setPublishAt(LocalDateTime.now());
        systemNoticeMapper.insert(notice);
        LOGGER.info("[ADMIN] 平台公告已发布, id={}", notice.getId());
        return notice;
    }

    public List<SystemNotice> listSystemNotices() {
        return systemNoticeMapper.selectList(new LambdaQueryWrapper<SystemNotice>()
                .orderByDesc(SystemNotice::getPublishAt));
    }

    /** 技能标签 */
    public Skill saveSkill(Skill skill) {
        if (skill.getId() == null) {
            skillMapper.insert(skill);
        } else {
            skillMapper.updateById(skill);
        }
        return skill;
    }

    public void deleteSkill(Long id) {
        skillMapper.deleteById(id);
    }
}
