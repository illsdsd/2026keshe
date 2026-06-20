package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campuslink.dto.TeamQuery;
import com.campuslink.dto.TeamListVO;
import com.campuslink.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 队伍 Mapper，含多维筛选分页查询。
 */
@Mapper
public interface TeamMapper extends BaseMapper<Team> {

    /**
     * 多维筛选分页：按竞赛、学院、年级（队长）、招募岗位（技能）、状态、关键词。
     */
    IPage<TeamListVO> pageTeams(IPage<TeamListVO> page, @Param("q") TeamQuery query);
}
