package com.liuh.gallerybackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/5/7 下午9:08
 * @PackageName com.liuh.gallerybackend.model.dto
 * @ClassName UserLoginRequest
 * @Version
 * @Description 用于前后端数据交换 -- 用户登录
 */

@SuppressWarnings("all")
@Data
public class UserLoginRequest implements Serializable {

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}
