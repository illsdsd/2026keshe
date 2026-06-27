package com.campuslink.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.entity.Task;
import com.campuslink.mapper.TaskMapper;
import com.campuslink.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 逾期任务提醒定时任务，v2 新增。
 *
 * <p>策略：每天 9 点扫描 deadline < now 且 status != DONE 的任务，
 * 给负责人发一条 TASK 类型站内消息提示「任务已逾期」。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Component
@RequiredArgsConstructor
public class TaskOverdueScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskOverdueScheduler.class);

    private final TaskMapper taskMapper;
    private final MessageService messageService;

    @Scheduled(cron = "${campuslink.schedule.task-overdue-cron:0 0 9 * * ?}")
    public void notifyOverdue() {
        LocalDateTime now = LocalDateTime.now();//当前时间
        LOGGER.info("[定时] 逾期任务提醒启动, now={}", now);

        List<Task> overdueTasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .lt(Task::getDeadline, now)
                .isNotNull(Task::getDeadline)
                .ne(Task::getStatus, "DONE")
                .isNotNull(Task::getAssigneeId));
        if (overdueTasks.isEmpty()) {
            LOGGER.info("[定时] 当前没有逾期任务");
            return;
        }

        int notified = 0;
        for (Task task : overdueTasks) {
            try {
                messageService.create(task.getAssigneeId(), "TASK",
                        "任务【" + task.getTitle() + "】已逾期，请尽快处理或联系队长调整截止时间",
                        task.getId());
                notified++;
                LOGGER.info("[定时] 逾期任务消息已发送, taskId={}, assigneeId={}", task.getId(), task.getAssigneeId());
            } catch (Exception e) {
                LOGGER.warn("[定时] 逾期任务消息发送失败, taskId={}", task.getId(), e);
            }
        }
        LOGGER.info("[定时] 逾期任务提醒结束, 已发送数量={}/{}", notified, overdueTasks.size());
    }
}
