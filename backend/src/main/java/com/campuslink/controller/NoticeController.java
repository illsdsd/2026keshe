package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.NoticeDTO;
import com.campuslink.dto.NoticeVO;
import com.campuslink.entity.Notice;
import com.campuslink.security.SecurityUtil;
import com.campuslink.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告接口：列表、发布、删除。
 */
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/team/{teamId}")
    public Result<List<NoticeVO>> listByTeam(@PathVariable Long teamId) {
        return Result.success(noticeService.listByTeam(teamId, SecurityUtil.getUserId()));
    }

    @PostMapping
    public Result<Notice> publish(@Valid @RequestBody NoticeDTO dto) {
        return Result.success(noticeService.publish(dto, SecurityUtil.getUserId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.delete(id, SecurityUtil.getUserId());
        return Result.success();
    }
}
