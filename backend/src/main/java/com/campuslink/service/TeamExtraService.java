package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.BusinessException;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.entity.FileObject;
import com.campuslink.entity.Team;
import com.campuslink.entity.TeamBlacklist;
import com.campuslink.entity.TeamFile;
import com.campuslink.entity.TeamMember;
import com.campuslink.entity.TeamPost;
import com.campuslink.entity.TeamPostComment;
import com.campuslink.entity.TeamPostLike;
import com.campuslink.entity.TeamRecruit;
import com.campuslink.mapper.FileObjectMapper;
import com.campuslink.mapper.TeamBlacklistMapper;
import com.campuslink.mapper.TeamFileMapper;
import com.campuslink.mapper.TeamMapper;
import com.campuslink.mapper.TeamMemberMapper;
import com.campuslink.mapper.TeamPostCommentMapper;
import com.campuslink.mapper.TeamPostLikeMapper;
import com.campuslink.mapper.TeamPostMapper;
import com.campuslink.mapper.TeamRecruitMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 队伍拓展业务（文件库 / 动态墙 / 副队长 / 归档 / 招募置顶 / 黑名单），v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class TeamExtraService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamExtraService.class);

    private final TeamFileMapper teamFileMapper;
    private final FileObjectMapper fileObjectMapper;
    private final TeamPostMapper teamPostMapper;
    private final TeamPostCommentMapper postCommentMapper;
    private final TeamPostLikeMapper postLikeMapper;
    private final TeamBlacklistMapper blacklistMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamMapper teamMapper;
    private final TeamRecruitMapper recruitMapper;
    private final MessageService messageService;

    /** 队伍文件库列表 */
    public List<TeamFile> listFiles(Long teamId, String folder, Long currentUserId) {
        ensureMember(teamId, currentUserId);
        LambdaQueryWrapper<TeamFile> wrapper = new LambdaQueryWrapper<TeamFile>()
                .eq(TeamFile::getTeamId, teamId)
                .orderByDesc(TeamFile::getCreateTime);
        if (folder != null && !folder.isBlank()) {
            wrapper.eq(TeamFile::getFolder, folder);
        }
        return teamFileMapper.selectList(wrapper);
    }

    public TeamFile addFile(Long teamId, Long fileId, String folder, String name, Long currentUserId) {
        ensureMember(teamId, currentUserId);
        if (fileObjectMapper.selectById(fileId) == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        TeamFile teamFile = new TeamFile();
        teamFile.setTeamId(teamId);
        teamFile.setFileId(fileId);
        teamFile.setFolder(folder == null ? "/" : folder);
        teamFile.setName(name);
        teamFile.setUploaderId(currentUserId);
        teamFileMapper.insert(teamFile);
        LOGGER.info("队伍文件已加入文件库, teamId={}, fileId={}, teamFileId={}", teamId, fileId, teamFile.getId());
        return teamFile;
    }

    public void deleteFile(Long teamFileId, Long currentUserId) {
        TeamFile teamFile = teamFileMapper.selectById(teamFileId);
        if (teamFile == null) {
            return;
        }
        TeamMember member = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamFile.getTeamId())
                .eq(TeamMember::getUserId, currentUserId));
        if (member == null) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        boolean canDelete = "LEADER".equals(member.getRole()) || "LEADER_DEPUTY".equals(member.getRole())
                || teamFile.getUploaderId().equals(currentUserId);
        if (canDelete) {
            teamFileMapper.deleteById(teamFileId);
            LOGGER.info("队伍文件已移除, teamFileId={}", teamFileId);
        } else {
            LOGGER.warn("队伍文件删除权限不足, userId={}, teamFileId={}", currentUserId, teamFileId);
            throw new BusinessException(ResultCode.FORBIDDEN, "只有队长/副队长/上传者可删除");
        }
    }

    /** 动态墙 */
    public Map<String, Object> pagePosts(Long teamId, Long currentUserId, long current, long size) {
        ensureMember(teamId, currentUserId);
        Page<TeamPost> page = teamPostMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<TeamPost>()
                        .eq(TeamPost::getTeamId, teamId)
                        .orderByDesc(TeamPost::getCreateTime));
        return PageResult.wrap(page);
    }

    public TeamPost publishPost(Long teamId, Long authorId, String content, String imageUrls) {
        ensureMember(teamId, authorId);
        TeamPost post = new TeamPost();
        post.setTeamId(teamId);
        post.setAuthorId(authorId);
        post.setContent(content);
        post.setImageUrls(imageUrls);
        post.setLikeCount(0);
        post.setCommentCount(0);
        teamPostMapper.insert(post);
        LOGGER.info("动态已发布, postId={}, teamId={}, authorId={}", post.getId(), teamId, authorId);
        return post;
    }

    public TeamPostComment commentPost(Long postId, Long authorId, String content) {
        TeamPost post = teamPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        ensureMember(post.getTeamId(), authorId);
        TeamPostComment comment = new TeamPostComment();
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setContent(content);
        postCommentMapper.insert(comment);
        post.setCommentCount(post.getCommentCount() + 1);
        teamPostMapper.updateById(post);
        return comment;
    }

    public boolean toggleLike(Long postId, Long userId) {
        TeamPost post = teamPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        ensureMember(post.getTeamId(), userId);

        TeamPostLike like = postLikeMapper.selectOne(new LambdaQueryWrapper<TeamPostLike>()
                .eq(TeamPostLike::getPostId, postId)
                .eq(TeamPostLike::getUserId, userId));
        if (like == null) {
            TeamPostLike newLike = new TeamPostLike();
            newLike.setPostId(postId);
            newLike.setUserId(userId);
            postLikeMapper.insert(newLike);
            post.setLikeCount(post.getLikeCount() + 1);
            teamPostMapper.updateById(post);
            LOGGER.info("动态点赞, postId={}, userId={}", postId, userId);
            return true;
        } else {
            postLikeMapper.deleteById(like.getId());
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            teamPostMapper.updateById(post);
            LOGGER.info("动态取消点赞, postId={}, userId={}", postId, userId);
            return false;
        }
    }

    /** 副队长任命 */
    public void promoteDeputy(Long teamMemberId, Long operatorId) {
        TeamMember member = teamMemberMapper.selectById(teamMemberId);
        if (member == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        Team team = teamMapper.selectById(member.getTeamId());
        if (team == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!team.getLeaderId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅队长可任命副队长");
        }
        if ("LEADER".equals(member.getRole())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "队长本人无需任命");
        }
        member.setRole("LEADER_DEPUTY");
        member.setDeputyTime(LocalDateTime.now());
        teamMemberMapper.updateById(member);
        LOGGER.info("副队长已任命, teamId={}, memberId={}", member.getTeamId(), teamMemberId);
        messageService.create(member.getUserId(), "NOTICE",
                "您被队长任命为队伍【" + team.getName() + "】的副队长", member.getTeamId());
    }

    /** 队伍归档 */
    public void archive(Long teamId, Long operatorId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!team.getLeaderId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅队长可归档队伍");
        }
        team.setStatus("ARCHIVED");
        team.setArchivedTime(LocalDateTime.now());
        teamMapper.updateById(team);
        LOGGER.info("队伍已归档, teamId={}", teamId);
    }

    /** 招募置顶（在 team_recruit 表上没有 top 字段，简化为把该岗位 sort 提升；这里直接用 filled 字段不变，
     *  通过把 count 增 1 模拟置顶不现实，故约定通过单独字段维护——简化实现：把 position 前加 ★。 */
    public TeamRecruit topRecruit(Long recruitId, Long operatorId) {
        TeamRecruit recruit = recruitMapper.selectById(recruitId);
        if (recruit == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        Team team = teamMapper.selectById(recruit.getTeamId());
        if (team == null || !team.getLeaderId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        String position = recruit.getPosition() == null ? "" : recruit.getPosition();
        if (!position.startsWith("★")) {
            recruit.setPosition("★ " + position);
            recruitMapper.updateById(recruit);
            LOGGER.info("招募已置顶, recruitId={}", recruitId);
        }
        return recruit;
    }

    /** 黑名单 */
    public TeamBlacklist blacklist(Long teamId, Long targetUserId, String reason, Long operatorId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!team.getLeaderId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅队长可拉黑");
        }
        Long exists = blacklistMapper.selectCount(new LambdaQueryWrapper<TeamBlacklist>()
                .eq(TeamBlacklist::getTeamId, teamId)
                .eq(TeamBlacklist::getUserId, targetUserId));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_RECORD, "该用户已在黑名单中");
        }
        TeamBlacklist bl = new TeamBlacklist();
        bl.setTeamId(teamId);
        bl.setUserId(targetUserId);
        bl.setReason(reason);
        bl.setOperatorId(operatorId);
        blacklistMapper.insert(bl);
        LOGGER.info("已加入黑名单, teamId={}, userId={}", teamId, targetUserId);
        return bl;
    }

    public List<TeamBlacklist> listBlacklist(Long teamId, Long operatorId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null || !team.getLeaderId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return blacklistMapper.selectList(new LambdaQueryWrapper<TeamBlacklist>()
                .eq(TeamBlacklist::getTeamId, teamId)
                .orderByDesc(TeamBlacklist::getCreateTime));
    }

    public void removeBlacklist(Long blacklistId, Long operatorId) {
        TeamBlacklist bl = blacklistMapper.selectById(blacklistId);
        if (bl == null) {
            return;
        }
        Team team = teamMapper.selectById(bl.getTeamId());
        if (team == null || !team.getLeaderId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        blacklistMapper.deleteById(blacklistId);
        LOGGER.info("黑名单已移除, blacklistId={}", blacklistId);
    }

    /** 公开方法：判定是否拉黑（供 ApplyService 调用） */
    public boolean isBlacklisted(Long teamId, Long userId) {
        Long cnt = blacklistMapper.selectCount(new LambdaQueryWrapper<TeamBlacklist>()
                .eq(TeamBlacklist::getTeamId, teamId)
                .eq(TeamBlacklist::getUserId, userId));
        return cnt != null && cnt > 0;
    }

    private void ensureMember(Long teamId, Long userId) {
        Long cnt = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId));
        if (cnt == null || cnt == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不是该队伍成员");
        }
    }
}
