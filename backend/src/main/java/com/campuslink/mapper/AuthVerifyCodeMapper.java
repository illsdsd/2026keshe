package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.AuthVerifyCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮箱验证码 Mapper，v2 新增。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Mapper
public interface AuthVerifyCodeMapper extends BaseMapper<AuthVerifyCode> {
}
