package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.UserPrivacy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户隐私 Mapper，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Mapper
public interface UserPrivacyMapper extends BaseMapper<UserPrivacy> {
}
