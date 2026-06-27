package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.FileObject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通用文件元数据 Mapper，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Mapper
public interface FileObjectMapper extends BaseMapper<FileObject> {
}
