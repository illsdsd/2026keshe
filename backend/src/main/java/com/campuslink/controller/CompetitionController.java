package com.campuslink.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campuslink.common.Result;
import com.campuslink.dto.CompetitionDTO;
import com.campuslink.entity.Competition;
import com.campuslink.security.SecurityUtil;
import com.campuslink.service.CompetitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 竞赛接口：列表筛选 + CRUD（写操作仅管理员，由 SecurityConfig 控制）。
 */
@RestController
@RequestMapping("/competition")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    @GetMapping
    public Result<IPage<Competition>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        return Result.success(competitionService.page(current, size, type, keyword));
    }

    @GetMapping("/{id}")
    public Result<Competition> getById(@PathVariable Long id) {
        return Result.success(competitionService.getById(id));
    }

    @PostMapping
    public Result<Competition> create(@Valid @RequestBody CompetitionDTO dto) {
        return Result.success(competitionService.create(dto, SecurityUtil.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<Competition> update(@PathVariable Long id, @Valid @RequestBody CompetitionDTO dto) {
        return Result.success(competitionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        competitionService.delete(id);
        return Result.success();
    }
}
