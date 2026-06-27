package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
