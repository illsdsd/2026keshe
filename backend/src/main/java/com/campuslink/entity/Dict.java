package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 数据字典实体，v2 新增。
 *
 * <p>type 标识字典分组（如 TASK_PRIORITY / COMP_TYPE / TEAM_ROLE），
 * code 为机内编码，label 为展示名。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Data
@TableName("dict")
public class Dict {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private String code;

    private String label;

    private Integer sort;

    private String remark;
}
