package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.UpdateEmailDTO;
import com.campuslink.dto.UpdatePasswordDTO;
import com.campuslink.dto.UserUpdateDTO;
import com.campuslink.entity.Skill;
import com.campuslink.entity.User;
import com.campuslink.entity.UserCertificate;
import com.campuslink.entity.UserFavorite;
import com.campuslink.entity.UserPrivacy;
import com.campuslink.entity.UserProject;
import com.campuslink.security.SecurityUtil;
import com.campuslink.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户接口：个人信息、技能、隐私、登录日志、作品集、证书、收藏。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<User> me() {
        return Result.success(userService.getById(SecurityUtil.getUserId()));
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PutMapping("/me")
    public Result<User> updateProfile(@Valid @RequestBody UserUpdateDTO dto) {
        return Result.success(userService.updateProfile(SecurityUtil.getUserId(), dto));
    }

    @GetMapping("/me/skills")
    public Result<List<Skill>> mySkills() {
        return Result.success(userService.listUserSkills(SecurityUtil.getUserId()));
    }

    @GetMapping("/{id}/skills")
    public Result<List<Skill>> userSkills(@PathVariable Long id) {
        return Result.success(userService.listUserSkills(id));
    }

    @PostMapping("/me/skills/{skillId}")
    public Result<Void> addSkill(@PathVariable Long skillId) {
        userService.addSkill(SecurityUtil.getUserId(), skillId);
        return Result.success();
    }

    @DeleteMapping("/me/skills/{skillId}")
    public Result<Void> removeSkill(@PathVariable Long skillId) {
        userService.removeSkill(SecurityUtil.getUserId(), skillId);
        return Result.success();
    }

    /**
     * 修改密码。
     */
    @PutMapping("/me/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        userService.updatePassword(SecurityUtil.getUserId(), dto);
        return Result.success();
    }

    /**
     * 更换邮箱。
     */
    @PutMapping("/me/email")
    public Result<Void> updateEmail(@Valid @RequestBody UpdateEmailDTO dto) {
        userService.updateEmail(SecurityUtil.getUserId(), dto);
        return Result.success();
    }

    @GetMapping("/me/privacy")
    public Result<UserPrivacy> getPrivacy() {
        return Result.success(userService.getPrivacy(SecurityUtil.getUserId()));
    }

    @PutMapping("/me/privacy")
    public Result<UserPrivacy> updatePrivacy(@RequestBody UserPrivacy patch) {
        return Result.success(userService.updatePrivacy(SecurityUtil.getUserId(), patch));
    }

    @GetMapping("/me/login-log")
    public Result<Map<String, Object>> loginLogs(@RequestParam(defaultValue = "1") long current,
                                                 @RequestParam(defaultValue = "10") long size) {
        return Result.success(userService.pageLoginLogs(SecurityUtil.getUserId(), current, size));
    }

    @GetMapping("/me/projects")
    public Result<List<UserProject>> myProjects() {
        return Result.success(userService.listProjects(SecurityUtil.getUserId()));
    }

    @GetMapping("/{id}/projects")
    public Result<List<UserProject>> userProjects(@PathVariable Long id) {
        return Result.success(userService.listProjects(id));
    }

    @PostMapping("/me/projects")
    public Result<UserProject> createProject(@RequestBody UserProject project) {
        project.setId(null);
        return Result.success(userService.saveProject(SecurityUtil.getUserId(), project));
    }

    @PutMapping("/me/projects/{id}")
    public Result<UserProject> updateProject(@PathVariable Long id, @RequestBody UserProject project) {
        project.setId(id);
        return Result.success(userService.saveProject(SecurityUtil.getUserId(), project));
    }

    @DeleteMapping("/me/projects/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        userService.deleteProject(SecurityUtil.getUserId(), id);
        return Result.success();
    }

    @GetMapping("/me/certificates")
    public Result<List<UserCertificate>> myCertificates() {
        return Result.success(userService.listCertificates(SecurityUtil.getUserId()));
    }

    @PostMapping("/me/certificates")
    public Result<UserCertificate> addCertificate(@RequestBody UserCertificate cert) {
        return Result.success(userService.addCertificate(SecurityUtil.getUserId(), cert));
    }

    @DeleteMapping("/me/certificates/{id}")
    public Result<Void> deleteCertificate(@PathVariable Long id) {
        userService.deleteCertificate(SecurityUtil.getUserId(), id);
        return Result.success();
    }

    @GetMapping("/me/favorites")
    public Result<List<UserFavorite>> myFavorites(@RequestParam(required = false) String refType) {
        return Result.success(userService.listFavorites(SecurityUtil.getUserId(), refType));
    }

    @PostMapping("/me/favorites")
    public Result<UserFavorite> addFavorite(@RequestBody UserFavorite favorite) {
        return Result.success(userService.addFavorite(SecurityUtil.getUserId(),
                favorite.getRefType(), favorite.getRefId(), favorite.getNote()));
    }

    @DeleteMapping("/me/favorites/{id}")
    public Result<Void> removeFavorite(@PathVariable Long id) {
        userService.removeFavorite(SecurityUtil.getUserId(), id);
        return Result.success();
    }
}
