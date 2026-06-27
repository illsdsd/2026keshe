package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 队伍文件库实体，v2 新增。
 *
 * <p>文件本身的物理路径与 mime 等元数据保存在 {@link FileObject}，本表只维护
 * 队伍 + 虚拟文件夹 + 文件 id 的映射，便于做权限判定与按队伍快速列表。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("team_file")
public class TeamFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private String folder;

    private Long fileId;

    private Long uploaderId;

    private String name;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
