package com.campuslink.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.entity.Team;
import com.campuslink.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 队伍归档定时任务，v2 新增。
 *
 * <p>策略：每天凌晨 3 点扫描 status=CLOSED 且 30 天未更新的队伍，
 * 将其状态切为 ARCHIVED，归档后队伍详情仍可查看但不可编辑。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Component
@RequiredArgsConstructor
public class TeamArchiveScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamArchiveScheduler.class);

    private final TeamMapper teamMapper;

    @Scheduled(cron = "${campuslink.schedule.team-archive-cron:0 0 3 * * ?}")
    public void archive() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);//30 天前
        LOGGER.info("[定时] 队伍归档扫描启动, threshold={}", threshold);

        List<Team> candidates = teamMapper.selectList(new LambdaQueryWrapper<Team>()
                .eq(Team::getStatus, "CLOSED")
                .lt(Team::getCreateTime, threshold));
        if (candidates.isEmpty()) {
            LOGGER.info("[定时] 没有需要归档的队伍");
            return;
        }

        int archived = 0;
        for (Team team : candidates) {
            team.setStatus("ARCHIVED");
            team.setArchivedTime(LocalDateTime.now());
            teamMapper.updateById(team);
            archived++;
            LOGGER.info("[定时] 队伍已归档, teamId={}", team.getId());
        }
        LOGGER.info("[定时] 队伍归档完成, 数量={}", archived);
    }
}
