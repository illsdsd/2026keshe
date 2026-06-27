package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.UserProject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户作品集 Mapper，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Mapper
public interface UserProjectMapper extends BaseMapper<UserProject> {
}
