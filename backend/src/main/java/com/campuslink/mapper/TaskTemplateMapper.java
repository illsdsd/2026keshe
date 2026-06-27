package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.TaskTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务模板 Mapper，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Mapper
public interface TaskTemplateMapper extends BaseMapper<TaskTemplate> {
}
