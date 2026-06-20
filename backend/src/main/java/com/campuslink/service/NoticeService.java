package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.common.BusinessException;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.NoticeDTO;
import com.campuslink.dto.NoticeVO;
import com.campuslink.entity.Notice;
import com.campuslink.entity.Team;
import com.campuslink.entity.TeamMember;
import com.campuslink.mapper.NoticeMapper;
import com.campuslink.mapper.TeamMapper;
import com.campuslink.mapper.TeamMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 公告服务：队长发布公告，成员查看，并向其余成员推送站内消息。
 */
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final MessageService messageService;

    /** 队伍成员查看公告 */
    public List<NoticeVO> listByTeam(Long teamId, Long operatorId) {
        requireMember(teamId, operatorId);
        return noticeMapper.listByTeam(teamId);
    }

    /** 队长发布公告 */
    @Transactional(rollbackFor = Exception.class)
    public Notice publish(NoticeDTO dto, Long operatorId) {
        Team team = teamMapper.selectById(dto.getTeamId());
        if (team == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "队伍不存在");
        }
        if (!team.getLeaderId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅队长可发布公告");
        }

        Notice notice = new Notice();
        notice.setTeamId(dto.getTeamId());
        notice.setAuthorId(operatorId);
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        noticeMapper.insert(notice);

        // 向除发布者外的成员推送站内消息
        List<TeamMember> members = teamMemberMapper.selectList(
                new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, dto.getTeamId()));
        for (TeamMember m : members) {
            if (!m.getUserId().equals(operatorId)) {
                messageService.create(m.getUserId(), "NOTICE",
                        String.format("队伍「%s」发布新公告：%s", team.getName(), dto.getTitle()),
                        notice.getId());
            }
        }
        return notice;
    }

    /** 删除公告（仅队长） */
    public void delete(Long id, Long operatorId) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在");
        }
        Team team = teamMapper.selectById(notice.getTeamId());
        if (team == null || !team.getLeaderId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅队长可删除公告");
        }
        noticeMapper.deleteById(id);
    }

    private void requireMember(Long teamId, Long userId) {
        Long count = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId));
        if (count == null || count == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "你不是该队伍成员");
        }
    }
}
