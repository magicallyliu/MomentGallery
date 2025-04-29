package com.liuh.gallerybackend.exception;

import lombok.Getter;

/**
 * @Author LiuH
 * @Date 2025/4/22 下午8:50
 * @PackageName com.liuh.gallerybackend.exception
 * @ClassName ErrorCode
 * @Version 1.0
 * @Description 错误码枚举类, 用于定义各类错误码
 */

@SuppressWarnings("all")
@Getter
public enum ErrorCode {

    SUCCESS(20001, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
