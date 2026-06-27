package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.ApplyDTO;
import com.campuslink.dto.ApplyVO;
import com.campuslink.dto.AuditDTO;
import com.campuslink.security.SecurityUtil;
import com.campuslink.service.ApplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 申请加入接口：提交、查看、审核。
 */
@RestController
@RequestMapping("/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final ApplyService applyService;

    /** 学生提交申请 */
    @PostMapping
    public Result<Void> apply(@Valid @RequestBody ApplyDTO dto) {
        applyService.apply(dto, SecurityUtil.getUserId());
        return Result.success();
    }

    /** 队长查看本队申请 */
    @GetMapping("/team/{teamId}")
    public Result<List<ApplyVO>> listByTeam(@PathVariable Long teamId) {
        return Result.success(applyService.listByTeam(teamId, SecurityUtil.getUserId()));
    }

    /** 我提交的申请 */
    @GetMapping("/mine")
    public Result<List<ApplyVO>> mine() {
        return Result.success(applyService.listMine(SecurityUtil.getUserId()));
    }

    /** 队长审核申请 */
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        applyService.audit(id, dto, SecurityUtil.getUserId());
        return Result.success();
    }

    /** v2 批量申请 */
    @PostMapping("/batch")
    public Result<java.util.Map<String, Object>> batch(@RequestBody List<ApplyDTO> dtos) {
        return Result.success(applyService.batchApply(dtos, SecurityUtil.getUserId()));
    }

    /** v2 申请撤回 */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        applyService.cancel(id, SecurityUtil.getUserId());
        return Result.success();
    }

    /** v2 申请通过率统计 */
    @GetMapping("/stat")
    public Result<java.util.Map<String, Object>> stat() {
        return Result.success(applyService.stat(SecurityUtil.getUserId()));
    }
}
