package com.liuh.gallerybackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/5/13 下午9:28
 * @PackageName com.liuh.gallerybackend.model.dto.user
 * @ClassName UserAddRequest
 * @Version
 * @Description 用户创建
 */

@SuppressWarnings("all")
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
