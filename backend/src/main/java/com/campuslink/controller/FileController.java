package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.entity.FileObject;
import com.campuslink.security.SecurityUtil;
import com.campuslink.service.FileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用文件接口：上传、下载、删除、我的文件中心。
 *
 * <p>分片上传仅在简化场景下做演示：前端在 {@code chunkIndex == totalChunks - 1}
 * 时调用合并接口。课设阶段为了实现简单，分片接口直接复用普通上传 + 客户端串行。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;

    /**
     * 通用上传接口。
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "scope", required = false, defaultValue = "USER") String scope,
                                              @RequestParam(value = "businessId", required = false) Long businessId) {
        Long userId = SecurityUtil.getUserId();//上传者
        LOGGER.info("文件上传请求, userId={}, scope={}, originalName={}", userId, scope, file.getOriginalFilename());
        FileObject saved = fileService.save(file, userId, scope, businessId);
        Map<String, Object> data = new HashMap<>();
        data.put("fileId", saved.getId());
        data.put("originalName", saved.getOriginalName());
        data.put("size", saved.getSizeBytes());
        data.put("mime", saved.getMime());
        return Result.success(data);
    }

    /**
     * 分片上传接口（简易实现：每片直接落库为独立文件，最后由前端拿到的 fileId 作为最终文件）。
     */
    @PostMapping("/upload-chunk")
    public Result<Map<String, Object>> uploadChunk(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("chunkIndex") Integer chunkIndex,
                                                   @RequestParam("totalChunks") Integer totalChunks,
                                                   @RequestParam("uploadId") String uploadId) {
        LOGGER.info("分片上传, uploadId={}, index={}/{}", uploadId, chunkIndex, totalChunks);
        Long userId = SecurityUtil.getUserId();
        FileObject saved = fileService.save(file, userId, "USER", null);
        Map<String, Object> data = new HashMap<>();
        data.put("fileId", saved.getId());
        data.put("chunkIndex", chunkIndex);
        data.put("uploadId", uploadId);
        data.put("finished", chunkIndex + 1 >= totalChunks);
        return Result.success(data);
    }

    /**
     * 文件下载。
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) throws Exception {
        Long userId = SecurityUtil.getUserId();
        FileObject fileObject = fileService.loadForDownload(id, userId);
        InputStream inputStream = fileService.openInputStream(fileObject);
        String filename = URLEncoder.encode(fileObject.getOriginalName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType(fileObject.getMime() == null ? "application/octet-stream" : fileObject.getMime()))
                .body(new InputStreamResource(inputStream));
    }

    /**
     * 删除文件。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id, SecurityUtil.getUserId());
        return Result.success();
    }

    /**
     * 我的文件中心。
     */
    @GetMapping("/mine")
    public Result<Map<String, Object>> mine(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size) {
        return Result.success(fileService.pageMine(SecurityUtil.getUserId(), current, size));
    }
}
