package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.common.BusinessException;
import com.campuslink.common.ExcelExportUtil;
import com.campuslink.common.ResultCode;
import com.campuslink.entity.Task;
import com.campuslink.entity.TaskComment;
import com.campuslink.entity.TaskTemplate;
import com.campuslink.entity.Team;
import com.campuslink.entity.TeamMember;
import com.campuslink.entity.Worklog;
import com.campuslink.mapper.TaskCommentMapper;
import com.campuslink.mapper.TaskMapper;
import com.campuslink.mapper.TaskTemplateMapper;
import com.campuslink.mapper.TeamMapper;
import com.campuslink.mapper.TeamMemberMapper;
import com.campuslink.mapper.WorklogMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 协作进阶业务（任务优先级 / 标签 / 评论 / 子任务 / 工时 / 模板），v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class TaskExtraService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExtraService.class);

    private final TaskMapper taskMapper;
    private final TaskCommentMapper taskCommentMapper;
    private final TaskTemplateMapper taskTemplateMapper;
    private final WorklogMapper worklogMapper;
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;

    /** 优先级 */
    public Task setPriority(Long taskId, String priority, Long operatorId) {
        Task task = ensureMemberOfTask(taskId, operatorId);
        if (!List.of("LOW", "MEDIUM", "HIGH").contains(priority)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法优先级");
        }
        task.setPriority(priority);
        taskMapper.updateById(task);
        LOGGER.info("任务优先级已更新, taskId={}, priority={}", taskId, priority);
        return task;
    }

    /** 标签 */
    public Task addTag(Long taskId, String tag, Long operatorId) {
        Task task = ensureMemberOfTask(taskId, operatorId);
        String current = task.getTags() == null ? "" : task.getTags();
        if (java.util.Arrays.stream(current.split(",")).noneMatch(t -> t.equals(tag))) {
            String merged = current.isBlank() ? tag : current + "," + tag;
            task.setTags(merged);
            taskMapper.updateById(task);
            LOGGER.info("任务标签已新增, taskId={}, tag={}", taskId, tag);
        }
        return task;
    }

    /** 评论 */
    public TaskComment comment(Long taskId, String content, Long authorId) {
        ensureMemberOfTask(taskId, authorId);
        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setAuthorId(authorId);
        comment.setContent(content);
        taskCommentMapper.insert(comment);
        return comment;
    }

    public List<TaskComment> listComments(Long taskId, Long operatorId) {
        ensureMemberOfTask(taskId, operatorId);
        return taskCommentMapper.selectList(new LambdaQueryWrapper<TaskComment>()
                .eq(TaskComment::getTaskId, taskId)
                .orderByAsc(TaskComment::getCreateTime));
    }

    /** 子任务 */
    public List<Task> listSubtasks(Long parentTaskId, Long operatorId) {
        ensureMemberOfTask(parentTaskId, operatorId);
        return taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getParentId, parentTaskId)
                .orderByAsc(Task::getSortOrder));
    }

    public Task createSubtask(Long parentTaskId, String title, Long assigneeId, Long operatorId) {
        Task parent = ensureMemberOfTask(parentTaskId, operatorId);
        Task sub = new Task();
        sub.setTeamId(parent.getTeamId());
        sub.setParentId(parentTaskId);
        sub.setTitle(title);
        sub.setAssigneeId(assigneeId);
        sub.setStatus("TODO");
        sub.setPriority(parent.getPriority() == null ? "MEDIUM" : parent.getPriority());
        sub.setSortOrder(0);
        taskMapper.insert(sub);
        LOGGER.info("子任务已新建, parentId={}, subId={}", parentTaskId, sub.getId());
        return sub;
    }

    public Task updateSubtaskStatus(Long subTaskId, String status, Long operatorId) {
        Task sub = taskMapper.selectById(subTaskId);
        if (sub == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        ensureMemberOf(sub.getTeamId(), operatorId);
        sub.setStatus(status);
        taskMapper.updateById(sub);

        //子任务全部 DONE 时主任务自动 DONE
        Long parentId = sub.getParentId();
        if (parentId != null) {
            refreshParentStatus(parentId);
        }
        return sub;
    }

    private void refreshParentStatus(Long parentId) {
        Task parent = taskMapper.selectById(parentId);
        if (parent == null) {
            return;
        }
        List<Task> subs = taskMapper.selectList(new LambdaQueryWrapper<Task>().eq(Task::getParentId, parentId));
        if (subs.isEmpty()) {
            return;
        }
        boolean allDone = subs.stream().allMatch(t -> "DONE".equals(t.getStatus()));
        if (allDone && !"DONE".equals(parent.getStatus())) {
            parent.setStatus("DONE");
            taskMapper.updateById(parent);
            LOGGER.info("主任务联动置为 DONE, taskId={}", parentId);
        }
    }

    /** 工时填报 */
    public Worklog logWork(Long taskId, BigDecimal hours, String content, LocalDate workDate, Long userId) {
        Task task = ensureMemberOfTask(taskId, userId);
        Worklog log = new Worklog();
        log.setTeamId(task.getTeamId());
        log.setTaskId(taskId);
        log.setUserId(userId);
        log.setHours(hours);
        log.setContent(content);
        log.setWorkDate(workDate == null ? LocalDate.now() : workDate);
        worklogMapper.insert(log);
        LOGGER.info("工时已填报, userId={}, taskId={}, hours={}", userId, taskId, hours);
        return log;
    }

    public List<Map<String, Object>> teamWorklogStat(Long teamId, Long operatorId) {
        ensureMemberOf(teamId, operatorId);
        List<Worklog> all = worklogMapper.selectList(new LambdaQueryWrapper<Worklog>().eq(Worklog::getTeamId, teamId));
        Map<Long, BigDecimal> grouped = new HashMap<>();
        for (Worklog log : all) {
            grouped.merge(log.getUserId(), log.getHours(), BigDecimal::add);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> e : grouped.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", e.getKey());
            item.put("hours", e.getValue());
            result.add(item);
        }
        return result;
    }

    /** 工时 Excel 导出 */
    public void exportWorklog(Long teamId, Long operatorId, HttpServletResponse response) throws IOException {
        Team team = teamMapper.selectById(teamId);
        if (team == null || !team.getLeaderId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅队长可导出工时");
        }
        List<Worklog> all = worklogMapper.selectList(new LambdaQueryWrapper<Worklog>()
                .eq(Worklog::getTeamId, teamId)
                .orderByAsc(Worklog::getWorkDate));
        List<List<Object>> rows = new ArrayList<>();
        for (Worklog log : all) {
            List<Object> row = new ArrayList<>();
            row.add(log.getId());
            row.add(log.getUserId());
            row.add(log.getTaskId());
            row.add(log.getWorkDate() == null ? "" : log.getWorkDate().toString());
            row.add(log.getHours());
            row.add(log.getContent());
            rows.add(row);
        }
        ExcelExportUtil.writeXlsx(response, "worklog-team-" + teamId,
                List.of("ID", "用户ID", "任务ID", "工作日期", "工时", "工作内容"),
                rows);
        LOGGER.info("工时已导出, teamId={}, 数量={}", teamId, all.size());
    }

    /** 任务模板 */
    public List<TaskTemplate> listTemplates(Long ownerId) {
        return taskTemplateMapper.selectList(new LambdaQueryWrapper<TaskTemplate>()
                .eq(TaskTemplate::getOwnerId, ownerId)
                .orderByDesc(TaskTemplate::getCreateTime));
    }

    public TaskTemplate saveTemplate(TaskTemplate template) {
        if (template.getId() == null) {
            taskTemplateMapper.insert(template);
            LOGGER.info("任务模板已新增, id={}", template.getId());
        } else {
            taskTemplateMapper.updateById(template);
            LOGGER.info("任务模板已更新, id={}", template.getId());
        }
        return template;
    }

    public void deleteTemplate(Long id, Long ownerId) {
        TaskTemplate template = taskTemplateMapper.selectById(id);
        if (template == null) {
            return;
        }
        if (!template.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        taskTemplateMapper.deleteById(id);
    }

    private Task ensureMemberOfTask(Long taskId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        ensureMemberOf(task.getTeamId(), userId);
        return task;
    }

    private void ensureMemberOf(Long teamId, Long userId) {
        Long cnt = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId));
        if (cnt == null || cnt == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不是该队伍成员");
        }
    }
}
