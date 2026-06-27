package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.TeamPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 队伍动态 Mapper，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Mapper
public interface TeamPostMapper extends BaseMapper<TeamPost> {
}
