package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.entity.Dict;
import com.campuslink.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据字典接口（前端 select 控件统一调用），v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    /**
     * 按类型查询字典。
     */
    @GetMapping("/{type}")
    public Result<List<Dict>> listByType(@PathVariable String type) {
        return Result.success(dictService.listByType(type));
    }

    /**
     * 列出全部字典（不分页，便于管理后台一次性渲染）。
     */
    @GetMapping("/all")
    public Result<List<Dict>> listAll() {
        return Result.success(dictService.listAll());
    }
}
