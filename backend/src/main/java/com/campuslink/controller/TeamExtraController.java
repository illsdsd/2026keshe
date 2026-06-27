package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.entity.TeamBlacklist;
import com.campuslink.entity.TeamFile;
import com.campuslink.entity.TeamPost;
import com.campuslink.entity.TeamPostComment;
import com.campuslink.entity.TeamRecruit;
import com.campuslink.security.SecurityUtil;
import com.campuslink.service.TeamExtraService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 队伍拓展接口：文件库 / 动态墙 / 副队长 / 归档 / 招募置顶 / 黑名单。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@RestController
@RequiredArgsConstructor
public class TeamExtraController {

    private final TeamExtraService teamExtraService;

    @GetMapping("/team/{id}/files")
    public Result<List<TeamFile>> files(@PathVariable("id") Long teamId,
                                        @RequestParam(required = false) String folder) {
        return Result.success(teamExtraService.listFiles(teamId, folder, SecurityUtil.getUserId()));
    }

    @PostMapping("/team/{id}/files")
    public Result<TeamFile> addFile(@PathVariable("id") Long teamId,
                                    @RequestBody Map<String, Object> body) {
        Long fileId = Long.valueOf(body.get("fileId").toString());
        String folder = (String) body.getOrDefault("folder", "/");
        String name = (String) body.get("name");
        return Result.success(teamExtraService.addFile(teamId, fileId, folder, name, SecurityUtil.getUserId()));
    }

    @DeleteMapping("/team-file/{id}")
    public Result<Void> deleteFile(@PathVariable Long id) {
        teamExtraService.deleteFile(id, SecurityUtil.getUserId());
        return Result.success();
    }

    @GetMapping("/team/{id}/posts")
    public Result<Map<String, Object>> posts(@PathVariable("id") Long teamId,
                                             @RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size) {
        return Result.success(teamExtraService.pagePosts(teamId, SecurityUtil.getUserId(), current, size));
    }

    @PostMapping("/team/{id}/posts")
    public Result<TeamPost> publishPost(@PathVariable("id") Long teamId,
                                        @RequestBody Map<String, Object> body) {
        String content = (String) body.get("content");
        String images = (String) body.get("imageUrls");
        return Result.success(teamExtraService.publishPost(teamId, SecurityUtil.getUserId(), content, images));
    }

    @PostMapping("/team-post/{id}/comment")
    public Result<TeamPostComment> commentPost(@PathVariable("id") Long postId,
                                               @RequestBody Map<String, Object> body) {
        String content = (String) body.get("content");
        return Result.success(teamExtraService.commentPost(postId, SecurityUtil.getUserId(), content));
    }

    @PostMapping("/team-post/{id}/like")
    public Result<Map<String, Object>> likePost(@PathVariable("id") Long postId) {
        boolean liked = teamExtraService.toggleLike(postId, SecurityUtil.getUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("liked", liked);
        return Result.success(data);
    }

    @PutMapping("/team-member/{id}/deputy")
    public Result<Void> promote(@PathVariable Long id) {
        teamExtraService.promoteDeputy(id, SecurityUtil.getUserId());
        return Result.success();
    }

    @PutMapping("/team/{id}/archive")
    public Result<Void> archive(@PathVariable("id") Long teamId) {
        teamExtraService.archive(teamId, SecurityUtil.getUserId());
        return Result.success();
    }

    @PutMapping("/team-recruit/{id}/top")
    public Result<TeamRecruit> topRecruit(@PathVariable Long id) {
        return Result.success(teamExtraService.topRecruit(id, SecurityUtil.getUserId()));
    }

    /** 拉黑 */
    @PostMapping("/team-blacklist")
    public Result<TeamBlacklist> blacklist(@RequestBody Map<String, Object> body) {
        Long teamId = Long.valueOf(body.get("teamId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());
        String reason = (String) body.getOrDefault("reason", "");
        return Result.success(teamExtraService.blacklist(teamId, userId, reason, SecurityUtil.getUserId()));
    }

    @GetMapping("/team/{id}/blacklist")
    public Result<List<TeamBlacklist>> listBlacklist(@PathVariable("id") Long teamId) {
        return Result.success(teamExtraService.listBlacklist(teamId, SecurityUtil.getUserId()));
    }

    @DeleteMapping("/team-blacklist/{id}")
    public Result<Void> removeBlacklist(@PathVariable Long id) {
        teamExtraService.removeBlacklist(id, SecurityUtil.getUserId());
        return Result.success();
    }
}
