package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.BusinessException;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.*;
import com.campuslink.entity.Competition;
import com.campuslink.entity.Team;
import com.campuslink.entity.TeamMember;
import com.campuslink.entity.TeamRecruit;
import com.campuslink.entity.User;
import com.campuslink.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 队伍服务：创建、浏览、多维筛选、招募岗位、成员管理。
 */
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamMapper teamMapper;
    private final TeamRecruitMapper teamRecruitMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final CompetitionMapper competitionMapper;
    private final UserMapper userMapper;

    /** 多维筛选分页 */
    public IPage<TeamListVO> page(TeamQuery query) {
        Page<TeamListVO> page = new Page<>(query.getCurrent(), query.getSize());
        return teamMapper.pageTeams(page, query);
    }

    /** 队伍详情 */
    public TeamDetailVO detail(Long id) {
        Team team = teamMapper.selectById(id);
        if (team == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "队伍不存在");
        }
        TeamDetailVO vo = new TeamDetailVO();
        vo.setTeam(team);
        if (team.getCompetitionId() != null) {
            Competition c = competitionMapper.selectById(team.getCompetitionId());
            vo.setCompetitionName(c != null ? c.getName() : null);
        }
        User leader = userMapper.selectById(team.getLeaderId());
        vo.setLeaderName(leader != null ? leader.getNickname() : null);
        vo.setRecruits(teamRecruitMapper.selectList(
                new LambdaQueryWrapper<TeamRecruit>().eq(TeamRecruit::getTeamId, id)));
        vo.setMembers(teamMemberMapper.listMembers(id));
        return vo;
    }

    /** 创建队伍：创建者成为队长并加入成员表 */
    @Transactional(rollbackFor = Exception.class)
    public Team create(TeamCreateDTO dto, Long leaderId) {
        if (dto.getCompetitionId() != null && competitionMapper.selectById(dto.getCompetitionId()) == null) {
            throw new BusinessException("关联竞赛不存在");
        }
        Team team = new Team();
        team.setName(dto.getName());
        team.setCompetitionId(dto.getCompetitionId());
        team.setLeaderId(leaderId);
        team.setIntro(dto.getIntro());
        team.setTotalSize(dto.getTotalSize());
        team.setCurrentSize(1);
        team.setCollege(dto.getCollege());
        team.setStatus("RECRUITING");
        teamMapper.insert(team);

        // 队长加入成员表
        TeamMember leader = new TeamMember();
        leader.setTeamId(team.getId());
        leader.setUserId(leaderId);
        leader.setRole("LEADER");
        teamMemberMapper.insert(leader);

        // 招募岗位
        if (dto.getRecruits() != null) {
            for (TeamCreateDTO.RecruitItem item : dto.getRecruits()) {
                TeamRecruit recruit = new TeamRecruit();
                recruit.setTeamId(team.getId());
                recruit.setPosition(item.getPosition());
                recruit.setCount(item.getCount() == null ? 1 : item.getCount());
                recruit.setFilled(0);
                teamRecruitMapper.insert(recruit);
            }
        }
        return team;
    }

    /** 编辑队伍（仅队长） */
    public Team update(Long id, TeamUpdateDTO dto, Long operatorId) {
        Team team = requireLeader(id, operatorId);
        if (dto.getName() != null) team.setName(dto.getName());
        if (dto.getCompetitionId() != null) team.setCompetitionId(dto.getCompetitionId());
        if (dto.getIntro() != null) team.setIntro(dto.getIntro());
        if (dto.getTotalSize() != null) team.setTotalSize(dto.getTotalSize());
        if (dto.getCollege() != null) team.setCollege(dto.getCollege());
        if (dto.getStatus() != null) team.setStatus(dto.getStatus());
        teamMapper.updateById(team);
        return team;
    }

    /** 解散队伍（仅队长） */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long operatorId) {
        requireLeader(id, operatorId);
        teamMemberMapper.delete(new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, id));
        teamRecruitMapper.delete(new LambdaQueryWrapper<TeamRecruit>().eq(TeamRecruit::getTeamId, id));
        teamMapper.deleteById(id);
    }

    /** 我创建 / 加入的队伍 */
    public List<Team> myTeams(Long userId) {
        List<TeamMember> members = teamMemberMapper.selectList(
                new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getUserId, userId));
        if (members.isEmpty()) {
            return List.of();
        }
        List<Long> teamIds = members.stream().map(TeamMember::getTeamId).toList();
        return teamMapper.selectBatchIds(teamIds);
    }

    private Team requireLeader(Long teamId, Long operatorId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "队伍不存在");
        }
        if (!team.getLeaderId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅队长可操作");
        }
        return team;
    }
}
