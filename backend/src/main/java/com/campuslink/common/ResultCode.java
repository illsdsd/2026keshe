package com.campuslink.common;

import lombok.Getter;

/**
 * 业务状态码。
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已失效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    ERROR(500, "服务器内部错误"),

    //账号安全
    ACCOUNT_LOCKED(4001, "账号已锁定，请稍后再试"),
    VERIFY_CODE_INVALID(4002, "验证码无效或已过期"),
    PASSWORD_NOT_MATCH(4003, "密码不正确"),

    //队伍/申请
    TEAM_BLACKLISTED(4101, "您已被该队伍拉黑，无法申请"),
    TEAM_ARCHIVED(4102, "队伍已归档，不可操作"),
    APPLY_NOT_PENDING(4103, "当前申请状态不可操作"),

    //文件
    FILE_TOO_LARGE(4201, "文件大小超出限制"),
    FILE_TYPE_INVALID(4202, "文件类型不允许"),
    FILE_NOT_FOUND(4203, "文件不存在"),

    //数据校验
    DUPLICATE_RECORD(4301, "记录已存在");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
