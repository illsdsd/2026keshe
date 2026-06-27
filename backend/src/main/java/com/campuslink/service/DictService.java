package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.entity.Dict;
import com.campuslink.mapper.DictMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据字典服务，v2 新增。
 *
 * <p>用于赛事类型、岗位、评价维度、任务优先级等枚举的统一维护，
 * 前端 select 控件按 type 拉取即可。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class DictService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DictService.class);

    private final DictMapper dictMapper;

    /**
     * 按 type 列出字典项。
     */
    public List<Dict> listByType(String type) {
        LOGGER.debug("查询字典, type={}", type);
        return dictMapper.selectList(new LambdaQueryWrapper<Dict>()
                .eq(Dict::getType, type)
                .orderByAsc(Dict::getSort));
    }

    /**
     * 列出所有字典（管理后台用）。
     */
    public List<Dict> listAll() {
        return dictMapper.selectList(new LambdaQueryWrapper<Dict>().orderByAsc(Dict::getType).orderByAsc(Dict::getSort));
    }

    /**
     * 新增字典。
     */
    public Dict create(Dict dict) {
        dictMapper.insert(dict);
        LOGGER.info("字典已新增, id={}, type={}, code={}", dict.getId(), dict.getType(), dict.getCode());
        return dict;
    }

    /**
     * 编辑字典。
     */
    public Dict update(Dict dict) {
        dictMapper.updateById(dict);
        LOGGER.info("字典已更新, id={}", dict.getId());
        return dict;
    }

    /**
     * 删除字典。
     */
    public void delete(Long id) {
        dictMapper.deleteById(id);
        LOGGER.info("字典已删除, id={}", id);
    }
}
