package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.CompetitionRegister;
import org.apache.ibatis.annotations.Mapper;

/**
 * 赛事报名 Mapper，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Mapper
public interface CompetitionRegisterMapper extends BaseMapper<CompetitionRegister> {
}
