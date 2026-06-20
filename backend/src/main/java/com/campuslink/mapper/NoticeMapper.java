package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.dto.NoticeVO;
import com.campuslink.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 公告 Mapper。
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    /** 队伍公告列表（含发布者昵称），按时间倒序 */
    @Select("""
            SELECT n.id, n.team_id AS teamId, n.author_id AS authorId, u.nickname AS authorName,
                   n.title, n.content, n.create_time AS createTime
            FROM notice n LEFT JOIN user u ON u.id = n.author_id
            WHERE n.team_id = #{teamId}
            ORDER BY n.create_time DESC
            """)
    List<NoticeVO> listByTeam(@Param("teamId") Long teamId);
}
