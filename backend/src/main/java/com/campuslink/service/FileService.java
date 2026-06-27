package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.BusinessException;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.config.FileStorageProperties;
import com.campuslink.entity.FileObject;
import com.campuslink.entity.TeamMember;
import com.campuslink.mapper.FileObjectMapper;
import com.campuslink.mapper.TeamMemberMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 通用文件存储服务。
 *
 * <p>本地磁盘实现：根目录由 {@link FileStorageProperties} 配置；元数据写
 * {@code file_object} 表。课设阶段不接入 OSS，但 service 内部已经按
 * 「保存路径」「下载流」「权限校验」分层，方便后续切换实现。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);
    private static final DateTimeFormatter DIR_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM");

    private final FileObjectMapper fileObjectMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final FileStorageProperties properties;

    /**
     * 保存上传文件并写元数据。
     *
     * @param file        上传文件
     * @param ownerId     上传者 id
     * @param scope       USER / TEAM / COMPETITION / PUBLIC
     * @param businessId  业务关联 id（可空）
     * @return 已落库的 FileObject
     */
    public FileObject save(MultipartFile file, Long ownerId, String scope, Long businessId) {
        //1 基础校验：非空 + 扩展名 + 大小
        if (file == null || file.isEmpty()) {
            LOGGER.warn("文件上传失败：空文件, ownerId={}", ownerId);
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件为空");
        }
        String originalName = file.getOriginalFilename();//原始文件名
        String extension = extractExt(originalName);//扩展名
        long sizeBytes = file.getSize();//文件大小
        long maxBytes = (properties.getMaxSizeMb() == null ? 50L : properties.getMaxSizeMb()) * 1024L * 1024L;
        boolean withinSize = sizeBytes <= maxBytes;//大小是否合规
        boolean extAllowed = isExtAllowed(extension);//扩展名是否在白名单
        if (withinSize && extAllowed) {//校验通过
            LOGGER.info("文件校验通过, name={}, size={}KB, ext={}", originalName, sizeBytes / 1024, extension);
        } else if (!withinSize) {//超出大小
            LOGGER.warn("文件大小超限, name={}, size={}KB, limit={}MB", originalName, sizeBytes / 1024, properties.getMaxSizeMb());
            throw new BusinessException(ResultCode.FILE_TOO_LARGE);
        } else {//扩展名非法
            LOGGER.warn("文件扩展名非法, name={}, ext={}", originalName, extension);
            throw new BusinessException(ResultCode.FILE_TYPE_INVALID);
        }

        //2 计算物理保存路径：根目录/yyyy/MM/uuid.ext
        String subDir = LocalDate.now().format(DIR_FORMAT);
        String fileName = UUID.randomUUID().toString().replace("-", "") + (extension == null ? "" : "." + extension);
        String relativePath = subDir + "/" + fileName;
        Path absolutePath = Paths.get(properties.getStoragePath(), subDir, fileName);

        //3 落盘
        try {
            Files.createDirectories(absolutePath.getParent());
            file.transferTo(absolutePath.toFile());
            LOGGER.info("文件已写入磁盘, path={}", absolutePath);
        } catch (IOException e) {
            LOGGER.error("文件写入磁盘失败, path={}", absolutePath, e);
            throw new BusinessException(ResultCode.ERROR, "文件写入失败");
        }

        //4 写元数据
        FileObject fileObject = new FileObject();
        fileObject.setOwnerId(ownerId);
        fileObject.setScope(scope == null ? "USER" : scope);
        fileObject.setBusinessId(businessId);
        fileObject.setOriginalName(originalName);
        fileObject.setStoredPath(relativePath);
        fileObject.setExtension(extension);
        fileObject.setMime(file.getContentType());
        fileObject.setSizeBytes(sizeBytes);
        fileObject.setDownloadCount(0);
        fileObjectMapper.insert(fileObject);
        LOGGER.info("文件元数据已写入, fileId={}", fileObject.getId());

        return fileObject;
    }

    /**
     * 获取文件元数据并校验下载权限。
     */
    public FileObject loadForDownload(Long fileId, Long currentUserId) {
        FileObject fileObject = fileObjectMapper.selectById(fileId);//查文件元数据
        if (fileObject == null) {
            LOGGER.warn("文件不存在, fileId={}", fileId);
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }

        boolean isOwner = fileObject.getOwnerId().equals(currentUserId);//是否所有者
        String scope = fileObject.getScope();//作用域
        boolean isPublic = "PUBLIC".equals(scope);//公开文件
        boolean isCompetition = "COMPETITION".equals(scope);//赛事附件直接登录可下
        boolean teamAccess = "TEAM".equals(scope) && isTeamMember(fileObject.getBusinessId(), currentUserId);//队伍成员可下

        if (isOwner || isPublic || isCompetition || teamAccess) {//有权限
            LOGGER.info("文件下载权限通过, fileId={}, userId={}, scope={}", fileId, currentUserId, scope);
            fileObjectMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<FileObject>()
                            .eq("id", fileId)
                            .setSql("download_count = download_count + 1"));
            return fileObject;
        } else {//无权限
            LOGGER.warn("文件下载权限不足, fileId={}, userId={}, scope={}", fileId, currentUserId, scope);
            throw new BusinessException(ResultCode.FORBIDDEN, "您没有权限下载该文件");
        }
    }

    /**
     * 删除文件（元数据 + 磁盘文件）。
     */
    public void delete(Long fileId, Long currentUserId) {
        FileObject fileObject = fileObjectMapper.selectById(fileId);
        if (fileObject == null) {
            LOGGER.warn("待删除文件不存在, fileId={}", fileId);
            return;
        }

        boolean isOwner = fileObject.getOwnerId().equals(currentUserId);//仅所有者可删
        if (isOwner) {//删除磁盘文件 + 元数据
            Path absolutePath = Paths.get(properties.getStoragePath(), fileObject.getStoredPath());
            try {
                Files.deleteIfExists(absolutePath);
                LOGGER.info("磁盘文件已删除, path={}", absolutePath);
            } catch (IOException e) {
                LOGGER.warn("磁盘文件删除异常, path={}", absolutePath, e);
            }
            fileObjectMapper.deleteById(fileId);
            LOGGER.info("文件元数据已删除, fileId={}", fileId);
        } else {
            LOGGER.warn("文件删除权限不足, fileId={}, userId={}", fileId, currentUserId);
            throw new BusinessException(ResultCode.FORBIDDEN, "只有上传者可以删除该文件");
        }
    }

    /**
     * 获取绝对磁盘路径，便于 Controller 输出流式响应。
     */
    public File resolveAbsoluteFile(FileObject fileObject) {
        return Paths.get(properties.getStoragePath(), fileObject.getStoredPath()).toFile();
    }

    /**
     * 我的文件中心分页查询。
     */
    public Map<String, Object> pageMine(Long ownerId, long current, long size) {
        Page<FileObject> page = new Page<>(current, size);
        Page<FileObject> result = fileObjectMapper.selectPage(page,
                new LambdaQueryWrapper<FileObject>()
                        .eq(FileObject::getOwnerId, ownerId)
                        .orderByDesc(FileObject::getCreateTime));
        return PageResult.wrap(result);
    }

    /**
     * 清理过期临时文件（定时任务调用）。
     *
     * @return 实际清理的文件条数
     */
    public int cleanExpired() {
        LocalDateTime now = LocalDateTime.now();//当前时间
        Long expiredCount = fileObjectMapper.selectCount(
                new LambdaQueryWrapper<FileObject>()
                        .lt(FileObject::getExpireAt, now)
                        .isNotNull(FileObject::getExpireAt));
        if (expiredCount == null || expiredCount == 0) {
            LOGGER.info("没有需要清理的过期文件");
            return 0;
        }

        java.util.List<FileObject> list = fileObjectMapper.selectList(
                new LambdaQueryWrapper<FileObject>()
                        .lt(FileObject::getExpireAt, now)
                        .isNotNull(FileObject::getExpireAt));
        int cleaned = 0;
        for (FileObject fileObject : list) {
            try {
                Path path = Paths.get(properties.getStoragePath(), fileObject.getStoredPath());
                Files.deleteIfExists(path);
                fileObjectMapper.deleteById(fileObject.getId());
                cleaned++;
            } catch (IOException e) {
                LOGGER.warn("过期文件清理失败, fileId={}", fileObject.getId(), e);
            }
        }
        LOGGER.info("过期文件清理完成, 数量={}", cleaned);
        return cleaned;
    }

    /**
     * 读取文件输入流（供 Controller 流式下载使用，需调用方手动关闭）。
     */
    public InputStream openInputStream(FileObject fileObject) throws IOException {
        File file = resolveAbsoluteFile(fileObject);
        if (!file.exists()) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return Files.newInputStream(file.toPath());
    }

    private String extractExt(String name) {
        if (name == null || !name.contains(".")) {
            return null;
        }
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }

    private boolean isExtAllowed(String ext) {
        if (ext == null) {
            return false;
        }
        String allowed = properties.getAllowedExtensions();
        if (allowed == null || allowed.isBlank()) {
            return true;
        }
        Set<String> allowSet = new HashSet<>(Arrays.asList(allowed.toLowerCase().split(",")));
        return allowSet.contains(ext);
    }

    private boolean isTeamMember(Long teamId, Long userId) {
        if (teamId == null) {
            return false;
        }
        Long count = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId));
        return count != null && count > 0;
    }
}
