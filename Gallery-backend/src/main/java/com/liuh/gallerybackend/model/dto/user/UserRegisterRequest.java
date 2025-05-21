package com.liuh.gallerybackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/5/6 下午5:58
 * @PackageName com.liuh.gallerybackend.model.dto
 * @ClassName user
 * @Version
 * @Description 用于前后端数据交换 -- 用户注册
 */

@SuppressWarnings("all")
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 4884086457375466192L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
