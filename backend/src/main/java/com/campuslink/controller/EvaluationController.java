package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.entity.Evaluation;
import com.campuslink.entity.EvaluationReply;
import com.campuslink.entity.Report;
import com.campuslink.security.SecurityUtil;
import com.campuslink.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评价接口（提交 / 回复 / 举报 / 信誉分明细 / 排行榜），v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@RestController
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/evaluation")
    public Result<Evaluation> submit(@RequestBody Map<String, Object> body) {
        Long teamId = Long.valueOf(body.get("teamId").toString());
        Long toUserId = Long.valueOf(body.get("toUserId").toString());
        int resp = ((Number) body.get("responsibility")).intValue();
        int tech = ((Number) body.get("tech")).intValue();
        int comm = ((Number) body.get("communication")).intValue();
        return Result.success(evaluationService.submit(teamId, SecurityUtil.getUserId(), toUserId, resp, tech, comm));
    }

    @GetMapping("/evaluation/user/{userId}")
    public Result<List<Evaluation>> listByUser(@PathVariable Long userId) {
        return Result.success(evaluationService.listByUser(userId));
    }

    @PostMapping("/evaluation/{id}/reply")
    public Result<EvaluationReply> reply(@PathVariable("id") Long evalId,
                                         @RequestBody Map<String, String> body) {
        return Result.success(evaluationService.reply(evalId, body.get("content"), SecurityUtil.getUserId()));
    }

    @GetMapping("/evaluation/{id}/replies")
    public Result<List<EvaluationReply>> replies(@PathVariable("id") Long evalId) {
        return Result.success(evaluationService.listReplies(evalId));
    }

    @PostMapping("/evaluation/{id}/report")
    public Result<Report> report(@PathVariable("id") Long evalId,
                                 @RequestBody Map<String, String> body) {
        return Result.success(evaluationService.report(evalId, body.get("reason"), SecurityUtil.getUserId()));
    }

    @GetMapping("/user/{id}/reputation-detail")
    public Result<Map<String, Object>> reputationDetail(@PathVariable("id") Long userId) {
        return Result.success(evaluationService.reputationDetail(userId));
    }

    @GetMapping("/reputation/ranking")
    public Result<List<Map<String, Object>>> ranking(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(evaluationService.ranking(limit));
    }
}
