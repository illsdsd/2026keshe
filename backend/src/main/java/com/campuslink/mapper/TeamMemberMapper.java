package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.dto.TeamMemberVO;
import com.campuslink.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 队伍成员 Mapper。
 */
@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {

    /**
     * 查询队伍成员（含用户昵称、头像、专业等）。
     */
    @Select("""
            SELECT tm.user_id AS userId, u.nickname, u.avatar, u.major, u.grade,
                   tm.role, tm.join_time AS joinTime
            FROM team_member tm JOIN user u ON u.id = tm.user_id
            WHERE tm.team_id = #{teamId}
            ORDER BY tm.role DESC, tm.join_time ASC
            """)
    List<TeamMemberVO> listMembers(@Param("teamId") Long teamId);
}
