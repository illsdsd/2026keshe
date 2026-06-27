package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通用文件元数据实体，v2 新增。
 *
 * <p>scope 取 USER / TEAM / COMPETITION / PUBLIC；
 * stored_path 是相对存储根目录的相对路径，物理根目录通过
 * 配置项 {@code campuslink.file.storage-path} 控制。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("file_object")
public class FileObject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ownerId;

    private String scope;

    private Long businessId;

    private String originalName;

    private String storedPath;

    private String extension;

    private String mime;

    private Long sizeBytes;

    private Integer downloadCount;

    private LocalDateTime expireAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
