package com.campuslink.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 分页返回体，与 v1 ApplyService / TeamService 中 Map 写法兼容，提供
 * 强类型版本便于新业务复用。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
public class PageResult<T> extends HashMap<String, Object> {

    public PageResult() {
    }

    public static <T> PageResult<T> of(java.util.List<T> records, long total, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.put("records", records);
        result.put("total", total);
        result.put("current", current);
        result.put("size", size);
        return result;
    }

    public static <T> PageResult<T> empty(long current, long size) {
        return of(java.util.Collections.emptyList(), 0L, current, size);
    }

    public static <T> Map<String, Object> wrap(com.baomidou.mybatisplus.core.metadata.IPage<T> page) {
        Map<String, Object> map = new HashMap<>();
        map.put("records", page.getRecords());
        map.put("total", page.getTotal());
        map.put("current", page.getCurrent());
        map.put("size", page.getSize());
        return map;
    }
}
