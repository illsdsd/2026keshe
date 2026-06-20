package com.campuslink.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campuslink.common.Result;
import com.campuslink.dto.*;
import com.campuslink.entity.Team;
import com.campuslink.security.SecurityUtil;
import com.campuslink.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 队伍接口：创建、浏览、多维筛选、详情、编辑、解散。
 */
@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    /** 多维筛选分页 */
    @GetMapping
    public Result<IPage<TeamListVO>> page(TeamQuery query) {
        return Result.success(teamService.page(query));
    }

    /** 我创建 / 加入的队伍 */
    @GetMapping("/mine")
    public Result<List<Team>> mine() {
        return Result.success(teamService.myTeams(SecurityUtil.getUserId()));
    }

    /** 队伍详情 */
    @GetMapping("/{id}")
    public Result<TeamDetailVO> detail(@PathVariable Long id) {
        return Result.success(teamService.detail(id));
    }

    /** 创建队伍 */
    @PostMapping
    public Result<Team> create(@Valid @RequestBody TeamCreateDTO dto) {
        return Result.success(teamService.create(dto, SecurityUtil.getUserId()));
    }

    /** 编辑队伍 */
    @PutMapping("/{id}")
    public Result<Team> update(@PathVariable Long id, @RequestBody TeamUpdateDTO dto) {
        return Result.success(teamService.update(id, dto, SecurityUtil.getUserId()));
    }

    /** 解散队伍 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        teamService.delete(id, SecurityUtil.getUserId());
        return Result.success();
    }
}
