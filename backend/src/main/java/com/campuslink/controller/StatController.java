package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.security.SecurityUtil;
import com.campuslink.service.StatService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * 数据统计接口，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@RestController
@RequestMapping("/stat")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @GetMapping("/user/me")
    public Result<Map<String, Object>> userStat() {
        return Result.success(statService.userStat(SecurityUtil.getUserId()));
    }

    @GetMapping("/user/{id}")
    public Result<Map<String, Object>> userStatById(@PathVariable Long id) {
        return Result.success(statService.userStat(id));
    }

    @GetMapping("/user/me/radar")
    public Result<Map<String, Object>> radar() {
        return Result.success(statService.userRadar(SecurityUtil.getUserId()));
    }

    @GetMapping("/team/{id}")
    public Result<Map<String, Object>> teamStat(@PathVariable("id") Long teamId) {
        return Result.success(statService.teamStat(teamId, SecurityUtil.getUserId()));
    }

    @GetMapping("/team/{id}/export")
    public void exportTeamStat(@PathVariable("id") Long teamId, HttpServletResponse response) throws IOException {
        statService.exportTeamStat(teamId, SecurityUtil.getUserId(), response);
    }

    @GetMapping("/competition/{id}")
    public Result<Map<String, Object>> competitionStat(@PathVariable("id") Long competitionId) {
        return Result.success(statService.competitionStat(competitionId));
    }

    @GetMapping("/platform/trends")
    public Result<Map<String, Object>> trends() {
        return Result.success(statService.platformTrends());
    }
}
