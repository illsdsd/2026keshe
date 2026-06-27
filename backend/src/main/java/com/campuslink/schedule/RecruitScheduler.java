package com.campuslink.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.entity.Competition;
import com.campuslink.entity.Team;
import com.campuslink.mapper.CompetitionMapper;
import com.campuslink.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 招募过期巡检定时任务，v2 新增。
 *
 * <p>策略：每半小时巡检一次，若队伍关联竞赛已过 deadline 且队伍状态仍为
 * {@code RECRUITING}，则将其改为 {@code CLOSED}，方便前端显示「招募已结束」。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Component
@RequiredArgsConstructor
public class RecruitScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecruitScheduler.class);

    private final TeamMapper teamMapper;
    private final CompetitionMapper competitionMapper;

    /**
     * 招募过期巡检。
     */
    @Scheduled(cron = "${campuslink.schedule.recruit-expire-cron:0 0/30 * * * ?}")
    public void scanExpired() {
        LocalDateTime now = LocalDateTime.now();//当前时间
        LOGGER.info("[定时] 招募过期巡检启动, now={}", now);

        //1 查所有已过 deadline 的竞赛
        List<Competition> expiredCompetitions = competitionMapper.selectList(
                new LambdaQueryWrapper<Competition>()
                        .lt(Competition::getDeadline, now)
                        .isNotNull(Competition::getDeadline));
        if (expiredCompetitions.isEmpty()) {
            LOGGER.info("[定时] 当前无过期竞赛");
            return;
        }
        LOGGER.info("[定时] 过期竞赛数量={}", expiredCompetitions.size());

        //2 关闭对应招募中队伍
        int closed = 0;
        for (Competition competition : expiredCompetitions) {
            List<Team> teams = teamMapper.selectList(new LambdaQueryWrapper<Team>()
                    .eq(Team::getCompetitionId, competition.getId())
                    .eq(Team::getStatus, "RECRUITING"));
            for (Team team : teams) {
                team.setStatus("CLOSED");
                teamMapper.updateById(team);
                closed++;
                LOGGER.info("[定时] 关闭过期招募, teamId={}, competitionId={}", team.getId(), competition.getId());
            }
        }
        LOGGER.info("[定时] 招募过期巡检结束, 关闭队伍数={}", closed);
    }
}
