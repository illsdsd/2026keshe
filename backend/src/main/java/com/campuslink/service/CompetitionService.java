package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.BusinessException;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.CompetitionDTO;
import com.campuslink.entity.Competition;
import com.campuslink.mapper.CompetitionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 竞赛服务：CRUD 与分页筛选。
 */
@Service
@RequiredArgsConstructor
public class CompetitionService {

    private final CompetitionMapper competitionMapper;

    public IPage<Competition> page(Integer current, Integer size, String type, String keyword) {
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq(Competition::getType, type);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Competition::getName, keyword);
        }
        wrapper.orderByDesc(Competition::getCreateTime);
        return competitionMapper.selectPage(new Page<>(current, size), wrapper);
    }

    public Competition getById(Long id) {
        Competition competition = competitionMapper.selectById(id);
        if (competition == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "竞赛不存在");
        }
        return competition;
    }

    public Competition create(CompetitionDTO dto, Long creatorId) {
        Competition competition = new Competition();
        competition.setName(dto.getName());
        competition.setType(dto.getType());
        competition.setIntro(dto.getIntro());
        competition.setDeadline(dto.getDeadline());
        competition.setCreatorId(creatorId);
        competitionMapper.insert(competition);
        return competition;
    }

    public Competition update(Long id, CompetitionDTO dto) {
        Competition competition = getById(id);
        competition.setName(dto.getName());
        competition.setType(dto.getType());
        competition.setIntro(dto.getIntro());
        competition.setDeadline(dto.getDeadline());
        competitionMapper.updateById(competition);
        return competition;
    }

    public void delete(Long id) {
        getById(id);
        competitionMapper.deleteById(id);
    }
}
