package com.liuh.gallerybackend.common;

import com.liuh.gallerybackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/4/23 下午9:45
 * @PackageName com.liuh.gallerybackend.common
 * @ClassName BaseResponse
 * @Version
 * @Description 全局响应封装类, 继承Serializable 表示支持序列化
 */

@SuppressWarnings("all")
@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 状态码
     */
    private int code;

    /**
     * 返回的数据
     */
    private T data;

    /**
     * 这个消息正确 或者 错误的原因
     */
    private String message;

    /**
     * 初始化一
     * 全部数据
     *
     * @param code    状态码
     * @param data    返回的数据
     * @param message 这个消息正确 或者 错误的原因
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 初始化二
     * 缺少原因
     *
     * @param code 状态码
     * @param data 返回的数据
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 初始化三
     * 使用已经封装好的错误码
     *
     * @param errorCode 错误码枚举类
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
