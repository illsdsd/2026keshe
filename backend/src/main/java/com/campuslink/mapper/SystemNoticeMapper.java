package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.SystemNotice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 平台全局公告 Mapper，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Mapper
public interface SystemNoticeMapper extends BaseMapper<SystemNotice> {
}
