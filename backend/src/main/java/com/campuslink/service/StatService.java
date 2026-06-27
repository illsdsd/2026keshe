package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.common.BusinessException;
import com.campuslink.common.ExcelExportUtil;
import com.campuslink.common.ResultCode;
import com.campuslink.entity.Apply;
import com.campuslink.entity.Evaluation;
import com.campuslink.entity.Task;
import com.campuslink.entity.Team;
import com.campuslink.entity.TeamMember;
import com.campuslink.entity.User;
import com.campuslink.entity.Worklog;
import com.campuslink.mapper.ApplyMapper;
import com.campuslink.mapper.EvaluationMapper;
import com.campuslink.mapper.TaskMapper;
import com.campuslink.mapper.TeamMapper;
import com.campuslink.mapper.TeamMemberMapper;
import com.campuslink.mapper.UserMapper;
import com.campuslink.mapper.WorklogMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计与可视化业务，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class StatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatService.class);

    private final UserMapper userMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamMapper teamMapper;
    private final TaskMapper taskMapper;
    private final ApplyMapper applyMapper;
    private final EvaluationMapper evaluationMapper;
    private final WorklogMapper worklogMapper;

    public Map<String, Object> userStat(Long userId) {
        Long competitionCount = teamMemberMapper.selectCount(
                new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getUserId, userId));
        Long doneTaskCount = taskMapper.selectCount(new LambdaQueryWrapper<Task>()
                .eq(Task::getAssigneeId, userId).eq(Task::getStatus, "DONE"));
        Long totalTaskCount = taskMapper.selectCount(new LambdaQueryWrapper<Task>()
                .eq(Task::getAssigneeId, userId));

        Long totalApply = applyMapper.selectCount(new LambdaQueryWrapper<Apply>().eq(Apply::getUserId, userId));
        Long approvedApply = applyMapper.selectCount(new LambdaQueryWrapper<Apply>()
                .eq(Apply::getUserId, userId).eq(Apply::getStatus, "APPROVED"));

        BigDecimal reputation = userMapper.selectById(userId).getReputation();
        double approveRate = totalApply == null || totalApply == 0 ? 0
                : (approvedApply == null ? 0 : approvedApply.doubleValue() / totalApply);

        Map<String, Object> data = new HashMap<>();
        data.put("competitionCount", competitionCount);
        data.put("doneTaskCount", doneTaskCount);
        data.put("totalTaskCount", totalTaskCount);
        data.put("reputation", reputation);
        data.put("approveRate", approveRate);
        return data;
    }

    /** 个人能力雷达图：从评价均值映射到 5 维 */
    public Map<String, Object> userRadar(Long userId) {
        List<Evaluation> evals = evaluationMapper.selectList(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getToUserId, userId));
        double resp = evals.isEmpty() ? 0 : evals.stream().mapToInt(Evaluation::getResponsibility).average().orElse(0);
        double tech = evals.isEmpty() ? 0 : evals.stream().mapToInt(Evaluation::getTech).average().orElse(0);
        double comm = evals.isEmpty() ? 0 : evals.stream().mapToInt(Evaluation::getCommunication).average().orElse(0);

        Long doneTaskCount = taskMapper.selectCount(new LambdaQueryWrapper<Task>()
                .eq(Task::getAssigneeId, userId).eq(Task::getStatus, "DONE"));
        Long competitionCount = teamMemberMapper.selectCount(
                new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getUserId, userId));

        // 把任务数与赛事数归一到 1-5
        double exec = Math.min(5, (doneTaskCount == null ? 0 : doneTaskCount) / 2.0);
        double collab = Math.min(5, (competitionCount == null ? 0 : competitionCount));

        Map<String, Object> data = new HashMap<>();
        data.put("dimensions", List.of("责任心", "技术能力", "沟通能力", "执行力", "协作面"));
        data.put("values", List.of(round(resp), round(tech), round(comm), round(exec), round(collab)));
        return data;
    }

    public Map<String, Object> teamStat(Long teamId, Long operatorId) {
        Long member = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId).eq(TeamMember::getUserId, operatorId));
        if (member == null || member == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不是该队伍成员");
        }
        Long total = taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getTeamId, teamId));
        Long done = taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getTeamId, teamId).eq(Task::getStatus, "DONE"));
        Long doing = taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getTeamId, teamId).eq(Task::getStatus, "DOING"));
        Long todo = taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getTeamId, teamId).eq(Task::getStatus, "TODO"));

        BigDecimal totalHours = BigDecimal.ZERO;
        List<Worklog> logs = worklogMapper.selectList(new LambdaQueryWrapper<Worklog>().eq(Worklog::getTeamId, teamId));
        for (Worklog log : logs) {
            totalHours = totalHours.add(log.getHours());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("done", done);
        data.put("doing", doing);
        data.put("todo", todo);
        data.put("totalHours", totalHours);
        data.put("doneRate", total == null || total == 0 ? 0 : (done == null ? 0 : done.doubleValue() / total));
        return data;
    }

    public void exportTeamStat(Long teamId, Long operatorId, HttpServletResponse response) throws IOException {
        Map<String, Object> stat = teamStat(teamId, operatorId);
        List<List<Object>> rows = new ArrayList<>();
        for (Map.Entry<String, Object> entry : stat.entrySet()) {
            rows.add(List.of(entry.getKey(), entry.getValue()));
        }
        ExcelExportUtil.writeXlsx(response, "team-stat-" + teamId, List.of("指标", "数值"), rows);
        LOGGER.info("[STAT] 队伍报表已导出, teamId={}", teamId);
    }

    public Map<String, Object> competitionStat(Long competitionId) {
        Long teamCount = teamMapper.selectCount(new LambdaQueryWrapper<Team>().eq(Team::getCompetitionId, competitionId));
        Long memberCount = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                .inSql(TeamMember::getTeamId, "SELECT id FROM team WHERE competition_id = " + competitionId));
        Map<String, Object> data = new HashMap<>();
        data.put("teamCount", teamCount);
        data.put("memberCount", memberCount);
        return data;
    }

    /** 平台趋势（近 7 天的新建队伍 / 任务 / 注册） */
    public Map<String, Object> platformTrends() {
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime next = day.plusDays(1);
            Long teams = teamMapper.selectCount(new LambdaQueryWrapper<Team>()
                    .ge(Team::getCreateTime, day).lt(Team::getCreateTime, next));
            Long tasks = taskMapper.selectCount(new LambdaQueryWrapper<Task>()
                    .ge(Task::getCreateTime, day).lt(Task::getCreateTime, next));
            Long users = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .ge(User::getCreateTime, day).lt(User::getCreateTime, next));
            Map<String, Object> point = new HashMap<>();
            point.put("date", day.toLocalDate().toString());
            point.put("teams", teams == null ? 0 : teams);
            point.put("tasks", tasks == null ? 0 : tasks);
            point.put("users", users == null ? 0 : users);
            trend.add(point);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("trend", trend);
        return data;
    }

    private double round(double v) {
        return Math.round(v * 100) / 100.0;
    }
}
