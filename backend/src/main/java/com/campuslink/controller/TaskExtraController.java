package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.entity.Task;
import com.campuslink.entity.TaskComment;
import com.campuslink.entity.TaskTemplate;
import com.campuslink.entity.Worklog;
import com.campuslink.security.SecurityUtil;
import com.campuslink.service.TaskExtraService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 协作进阶接口（任务优先级 / 评论 / 子任务 / 工时 / 模板），v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@RestController
@RequiredArgsConstructor
public class TaskExtraController {

    private final TaskExtraService taskExtraService;

    @PutMapping("/task/{id}/priority")
    public Result<Task> setPriority(@PathVariable("id") Long taskId,
                                    @RequestBody Map<String, String> body) {
        return Result.success(taskExtraService.setPriority(taskId, body.get("priority"), SecurityUtil.getUserId()));
    }

    @PostMapping("/task/{id}/tag")
    public Result<Task> addTag(@PathVariable("id") Long taskId,
                               @RequestBody Map<String, String> body) {
        return Result.success(taskExtraService.addTag(taskId, body.get("tag"), SecurityUtil.getUserId()));
    }

    @PostMapping("/task/{id}/comment")
    public Result<TaskComment> comment(@PathVariable("id") Long taskId,
                                       @RequestBody Map<String, String> body) {
        return Result.success(taskExtraService.comment(taskId, body.get("content"), SecurityUtil.getUserId()));
    }

    @GetMapping("/task/{id}/comments")
    public Result<List<TaskComment>> listComments(@PathVariable("id") Long taskId) {
        return Result.success(taskExtraService.listComments(taskId, SecurityUtil.getUserId()));
    }

    @GetMapping("/subtask/task/{parentId}")
    public Result<List<Task>> listSubtasks(@PathVariable Long parentId) {
        return Result.success(taskExtraService.listSubtasks(parentId, SecurityUtil.getUserId()));
    }

    @PostMapping("/subtask")
    public Result<Task> createSubtask(@RequestBody Map<String, Object> body) {
        Long parentId = Long.valueOf(body.get("parentId").toString());
        String title = (String) body.get("title");
        Long assignee = body.get("assigneeId") == null ? null : Long.valueOf(body.get("assigneeId").toString());
        return Result.success(taskExtraService.createSubtask(parentId, title, assignee, SecurityUtil.getUserId()));
    }

    @PutMapping("/subtask/{id}")
    public Result<Task> updateSubtaskStatus(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        return Result.success(taskExtraService.updateSubtaskStatus(id, body.get("status"), SecurityUtil.getUserId()));
    }

    @PostMapping("/worklog")
    public Result<Worklog> logWork(@RequestBody Map<String, Object> body) {
        Long taskId = Long.valueOf(body.get("taskId").toString());
        BigDecimal hours = new BigDecimal(body.get("hours").toString());
        String content = (String) body.getOrDefault("content", "");
        String workDateStr = (String) body.get("workDate");
        LocalDate workDate = workDateStr == null ? null : LocalDate.parse(workDateStr);
        return Result.success(taskExtraService.logWork(taskId, hours, content, workDate, SecurityUtil.getUserId()));
    }

    @GetMapping("/worklog/team/{teamId}/stat")
    public Result<List<Map<String, Object>>> teamWorklogStat(@PathVariable Long teamId) {
        return Result.success(taskExtraService.teamWorklogStat(teamId, SecurityUtil.getUserId()));
    }

    @GetMapping("/worklog/team/{teamId}/export")
    public void exportWorklog(@PathVariable Long teamId, HttpServletResponse response) throws IOException {
        taskExtraService.exportWorklog(teamId, SecurityUtil.getUserId(), response);
    }

    @GetMapping("/task/template")
    public Result<List<TaskTemplate>> listTemplates() {
        return Result.success(taskExtraService.listTemplates(SecurityUtil.getUserId()));
    }

    @PostMapping("/task/template")
    public Result<TaskTemplate> saveTemplate(@RequestBody TaskTemplate template) {
        template.setOwnerId(SecurityUtil.getUserId());
        return Result.success(taskExtraService.saveTemplate(template));
    }

    @DeleteMapping("/task/template/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        taskExtraService.deleteTemplate(id, SecurityUtil.getUserId());
        return Result.success();
    }
}
