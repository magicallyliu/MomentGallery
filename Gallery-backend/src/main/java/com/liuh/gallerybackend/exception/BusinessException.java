package com.liuh.gallerybackend.exception;

import lombok.Data;

/**
 * @Author LiuH
 * @Date 2025/4/23 下午8:57
 * @PackageName com.liuh.gallerybackend.exception
 * @ClassName BusinessException
 * @Version
 * @Description 业务异常
 */

@SuppressWarnings("all")
@Data
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     *
     */
    private final int code;

    /**
     * 初始化方法一
     *  自定义错误码
     * @param code 错误码
     * @param message  原因
     */
    public BusinessException(int code, String message) {
        super(message); //将错误原因提供
        this.code = code;
    }

    /**
     * 初始化方法二
     * @param errorCode 已经封装完毕的错误码
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 初始化方法三
     * 需要重新书写错误码的错误原因
     * @param errorCode
     * @param message 重新提供的错误原因
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
