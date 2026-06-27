package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.Dict;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据字典 Mapper，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Mapper
public interface DictMapper extends BaseMapper<Dict> {
}
