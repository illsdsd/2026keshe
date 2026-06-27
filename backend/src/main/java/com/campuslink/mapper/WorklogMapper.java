package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.Worklog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工时填报 Mapper，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Mapper
public interface WorklogMapper extends BaseMapper<Worklog> {
}
