package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.Evaluation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 互评 Mapper，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {
}
