package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.entity.Report;
import com.campuslink.entity.Skill;
import com.campuslink.entity.SystemNotice;
import com.campuslink.security.SecurityUtil;
import com.campuslink.service.AdminService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 管理后台接口，v2 新增。
 *
 * <p>所有路径 {@code /admin/**} 在 SecurityConfig 中由 ROLE_ADMIN 兜底保护。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public Result<Map<String, Object>> users(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String role,
                                             @RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size) {
        return Result.success(adminService.pageUsers(keyword, role, current, size));
    }

    @PutMapping("/users/{id}/disable")
    public Result<Void> disableUser(@PathVariable Long id) {
        adminService.disableUser(id);
        return Result.success();
    }

    @PutMapping("/users/{id}/enable")
    public Result<Void> enableUser(@PathVariable Long id) {
        adminService.enableUser(id);
        return Result.success();
    }

    @PutMapping("/users/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.resetPassword(id, body.getOrDefault("newPassword", "123456"));
        return Result.success();
    }

    @GetMapping("/users/export")
    public void exportUsers(HttpServletResponse response) throws IOException {
        adminService.exportUsers(response);
    }

    @GetMapping("/teams")
    public Result<Map<String, Object>> teams(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size) {
        return Result.success(adminService.pageTeams(keyword, status, current, size));
    }

    @DeleteMapping("/teams/{id}")
    public Result<Void> disband(@PathVariable Long id) {
        adminService.disbandTeam(id);
        return Result.success();
    }

    @GetMapping("/reports")
    public Result<Map<String, Object>> reports(@RequestParam(required = false) String status,
                                               @RequestParam(defaultValue = "1") long current,
                                               @RequestParam(defaultValue = "10") long size) {
        return Result.success(adminService.pageReports(status, current, size));
    }

    @PutMapping("/reports/{id}/handle")
    public Result<Report> handleReport(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Boolean handled = (Boolean) body.getOrDefault("handled", Boolean.TRUE);
        String remark = (String) body.getOrDefault("remark", "");
        return Result.success(adminService.handleReport(id, Boolean.TRUE.equals(handled), remark, SecurityUtil.getUserId()));
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.success(adminService.dashboard());
    }

    @PostMapping("/notice")
    public Result<SystemNotice> publish(@RequestBody Map<String, String> body) {
        return Result.success(adminService.publishSystemNotice(body.get("title"), body.get("content"), SecurityUtil.getUserId()));
    }

    @GetMapping("/notice")
    public Result<List<SystemNotice>> listNotices() {
        return Result.success(adminService.listSystemNotices());
    }

    @PostMapping("/skill")
    public Result<Skill> createSkill(@RequestBody Skill skill) {
        skill.setId(null);
        return Result.success(adminService.saveSkill(skill));
    }

    @PutMapping("/skill/{id}")
    public Result<Skill> updateSkill(@PathVariable Long id, @RequestBody Skill skill) {
        skill.setId(id);
        return Result.success(adminService.saveSkill(skill));
    }

    @DeleteMapping("/skill/{id}")
    public Result<Void> deleteSkill(@PathVariable Long id) {
        adminService.deleteSkill(id);
        return Result.success();
    }
}
