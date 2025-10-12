package com.liuh.gallerybackend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.liuh.gallerybackend.common.BaseResponse;
import com.liuh.gallerybackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author LiuH
 * @Date 2025/4/24 下午9:22
 * @PackageName com.liuh.gallerybackend.exception
 * @ClassName GlobalExceptionHandler
 * @Version
 * @Description 全局异常处理器
 */

@SuppressWarnings("all")
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 发生已封装好的业务异常种类时, 进行调用, 用于返回前端
     *
     * @param e 业务异常
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("发生的业务异常: ", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 系统发生运行异常时调用
     *
     * @param e 系统运行异常
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> RuntimeExceptionHandler(RuntimeException e) {
        log.error("发生运行异常: ", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统运行异常");
    }

    /**
     * 用于捕获sa-token的未登录异常
     * @param e
     * @return
     */
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginException(NotLoginException e) {
        log.error("NotLoginException", e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
    }

    /**
     *  用于捕获sa-token的未授权异常
     * @param e
     * @return
     */
    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> notPermissionExceptionHandler(NotPermissionException e) {
        log.error("NotPermissionException", e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, e.getMessage());
    }

}
