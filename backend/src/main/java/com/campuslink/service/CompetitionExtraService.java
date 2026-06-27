package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.BusinessException;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.entity.Competition;
import com.campuslink.entity.CompetitionAttachment;
import com.campuslink.entity.CompetitionNews;
import com.campuslink.entity.CompetitionRegister;
import com.campuslink.entity.Team;
import com.campuslink.mapper.CompetitionAttachmentMapper;
import com.campuslink.mapper.CompetitionMapper;
import com.campuslink.mapper.CompetitionNewsMapper;
import com.campuslink.mapper.CompetitionRegisterMapper;
import com.campuslink.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 赛事拓展业务（报名 / 附件 / 资讯 / 排行榜），v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class CompetitionExtraService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompetitionExtraService.class);

    private final CompetitionRegisterMapper registerMapper;
    private final CompetitionAttachmentMapper attachmentMapper;
    private final CompetitionNewsMapper newsMapper;
    private final CompetitionMapper competitionMapper;
    private final TeamMapper teamMapper;
    private final MessageService messageService;

    /**
     * 队伍报名竞赛。
     */
    public CompetitionRegister register(Long competitionId, Long teamId, Long applicantId, String remark) {
        //1 校验竞赛
        Competition competition = competitionMapper.selectById(competitionId);
        if (competition == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "竞赛不存在");
        }
        if (competition.getDeadline() != null && competition.getDeadline().isBefore(LocalDateTime.now())) {
            LOGGER.warn("报名失败：竞赛已截止, competitionId={}", competitionId);
            throw new BusinessException(ResultCode.BAD_REQUEST, "竞赛报名已截止");
        }

        //2 校验队伍归属（队长才能报名）
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "队伍不存在");
        }
        if (!team.getLeaderId().equals(applicantId)) {
            LOGGER.warn("报名失败：非队长操作, teamId={}, applicantId={}", teamId, applicantId);
            throw new BusinessException(ResultCode.FORBIDDEN, "仅队长可代表队伍报名");
        }
        if ("ARCHIVED".equals(team.getStatus())) {
            throw new BusinessException(ResultCode.TEAM_ARCHIVED);
        }

        //3 防重
        Long exists = registerMapper.selectCount(new LambdaQueryWrapper<CompetitionRegister>()
                .eq(CompetitionRegister::getCompetitionId, competitionId)
                .eq(CompetitionRegister::getTeamId, teamId));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_RECORD, "队伍已经报名过该竞赛");
        }

        //4 写报名记录
        CompetitionRegister entity = new CompetitionRegister();
        entity.setCompetitionId(competitionId);
        entity.setTeamId(teamId);
        entity.setApplicantId(applicantId);
        entity.setRemark(remark);
        entity.setStatus("PENDING");
        registerMapper.insert(entity);
        LOGGER.info("队伍报名完成, registerId={}, competitionId={}, teamId={}", entity.getId(), competitionId, teamId);

        //5 推送站内消息给队长（仅本人，便于在「我的消息」可见）
        messageService.create(applicantId, "AUDIT",
                "已为队伍【" + team.getName() + "】提交赛事【" + competition.getName() + "】报名，等待管理员审核",
                entity.getId());
        return entity;
    }

    /**
     * 审核报名（管理员）。
     */
    public CompetitionRegister audit(Long registerId, boolean approved, String reason) {
        CompetitionRegister register = registerMapper.selectById(registerId);
        if (register == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!"PENDING".equals(register.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "报名状态不可审核");
        }
        register.setStatus(approved ? "APPROVED" : "REJECTED");
        register.setAuditReason(reason);
        register.setAuditTime(LocalDateTime.now());
        registerMapper.updateById(register);
        LOGGER.info("赛事报名审核完成, registerId={}, approved={}", registerId, approved);

        Team team = teamMapper.selectById(register.getTeamId());
        if (team != null) {
            messageService.create(team.getLeaderId(), "AUDIT",
                    (approved ? "审核通过：" : "审核拒绝：") + (reason == null ? "" : reason),
                    registerId);
        }
        return register;
    }

    /**
     * 报名列表（管理员 / 队长视角）。
     */
    public Map<String, Object> pageRegisters(Long competitionId, String status, long current, long size) {
        LambdaQueryWrapper<CompetitionRegister> wrapper = new LambdaQueryWrapper<CompetitionRegister>()
                .eq(CompetitionRegister::getCompetitionId, competitionId)
                .orderByDesc(CompetitionRegister::getCreateTime);
        if (status != null && !status.isBlank()) {
            wrapper.eq(CompetitionRegister::getStatus, status);
        }
        Page<CompetitionRegister> page = registerMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.wrap(page);
    }

    /**
     * 赛事附件列表。
     */
    public List<CompetitionAttachment> listAttachments(Long competitionId) {
        return attachmentMapper.selectList(new LambdaQueryWrapper<CompetitionAttachment>()
                .eq(CompetitionAttachment::getCompetitionId, competitionId)
                .orderByDesc(CompetitionAttachment::getCreateTime));
    }

    public CompetitionAttachment addAttachment(Long competitionId, Long fileId, String name, String category) {
        if (competitionMapper.selectById(competitionId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "竞赛不存在");
        }
        CompetitionAttachment attachment = new CompetitionAttachment();
        attachment.setCompetitionId(competitionId);
        attachment.setFileId(fileId);
        attachment.setName(name);
        attachment.setCategory(category == null ? "OTHER" : category);
        attachmentMapper.insert(attachment);
        LOGGER.info("赛事附件已上传, attachmentId={}, competitionId={}", attachment.getId(), competitionId);
        return attachment;
    }

    public void deleteAttachment(Long attachmentId) {
        attachmentMapper.deleteById(attachmentId);
        LOGGER.info("赛事附件已删除, attachmentId={}", attachmentId);
    }

    /**
     * 资讯。
     */
    public List<CompetitionNews> listNews(Long competitionId) {
        return newsMapper.selectList(new LambdaQueryWrapper<CompetitionNews>()
                .eq(CompetitionNews::getCompetitionId, competitionId)
                .orderByDesc(CompetitionNews::getCreateTime));
    }

    public CompetitionNews publishNews(Long competitionId, String title, String content, Long authorId) {
        if (competitionMapper.selectById(competitionId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "竞赛不存在");
        }
        CompetitionNews news = new CompetitionNews();
        news.setCompetitionId(competitionId);
        news.setTitle(title);
        news.setContent(content);
        news.setAuthorId(authorId);
        newsMapper.insert(news);
        LOGGER.info("赛事资讯已发布, newsId={}, competitionId={}", news.getId(), competitionId);
        return news;
    }

    /**
     * 赛事排行榜（统计每个竞赛累计 APPROVED 报名数）。
     */
    public List<Map<String, Object>> ranking() {
        List<Competition> competitions = competitionMapper.selectList(null);
        java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Competition comp : competitions) {
            Long approvedCount = registerMapper.selectCount(new LambdaQueryWrapper<CompetitionRegister>()
                    .eq(CompetitionRegister::getCompetitionId, comp.getId())
                    .eq(CompetitionRegister::getStatus, "APPROVED"));
            Map<String, Object> item = new HashMap<>();
            item.put("competitionId", comp.getId());
            item.put("competitionName", comp.getName());
            item.put("type", comp.getType());
            item.put("approvedCount", approvedCount == null ? 0 : approvedCount);
            result.add(item);
        }
        result.sort((a, b) -> Long.compare(((Number) b.get("approvedCount")).longValue(),
                ((Number) a.get("approvedCount")).longValue()));
        return result;
    }
}
