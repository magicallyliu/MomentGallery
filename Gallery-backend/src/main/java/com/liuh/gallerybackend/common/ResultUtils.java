package com.liuh.gallerybackend.common;

import com.liuh.gallerybackend.exception.ErrorCode;

/**
 * @Author LiuH
 * @Date 2025/4/24 下午8:45
 * @PackageName com.liuh.gallerybackend.common
 * @ClassName ResultUtils
 * @Version
 * @Description 用于封装提供成功调用和失败调用的方法
 */

@SuppressWarnings("all")

public class ResultUtils {

    /**
     * 成功
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(20001, data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode 已经封装好的错误码
     * @param <T>       数据类型
     * @return 响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * 自定义错误码
     * @param code 错误码
     * @param message 错误原图
     * @return 响应
     * @param <T> 数据类型
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code,null, message);
    }

    /**
     * 失败
     * 使用自定义错误码, 但是重新书写错误的原因
     * @param errorCode 已经封装好的错误码
     * @param message 错误的原因
     * @return 响应
     * @param <T> 数据类型
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(),null, message);
    }
}
