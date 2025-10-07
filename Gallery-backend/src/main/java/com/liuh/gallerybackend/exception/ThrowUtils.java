package com.liuh.gallerybackend.exception;

/**
 * @Author LiuH
 * @Date 2025/4/23 下午9:20
 * @PackageName com.liuh.gallerybackend.exception
 * @ClassName ThrowUils
 * @Version
 * @Description 异常处理工具类
 */

@SuppressWarnings("all")

public class ThrowUtils {

    /**
     * 判断条件成立抛出异常
     * @param condition 判断条件得到的true/false
     * @param runtimeException 业务异常 (是 BusinessException类 的父类)
     */
    public static void throwIf(Boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     *  条件成立抛出异常
     *  使用已封装的错误码
     * @param condition 判断条件得到的true/false
     * @param errorCode 已封装的错误码
     */
    public static void throwIf(Boolean condition, ErrorCode errorCode) {
        throwIf(condition,new BusinessException(errorCode));
    }

    /**
     * 条件成立抛出异常
     * 使用已封装完毕的错误码, 但提供了重新错误码的原因
     * @param condition 判断条件得到的true/false
     * @param errorCode 已封装的错误码
     * @param message 新错误码的原因
     */
    public static void throwIf(Boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition,new BusinessException(errorCode,message));
    }
}
