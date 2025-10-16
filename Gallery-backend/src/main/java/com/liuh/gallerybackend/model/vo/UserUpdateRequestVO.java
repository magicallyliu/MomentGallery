package com.liuh.gallerybackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/5/13 下午9:30
 * @PackageName com.liuh.gallerybackend.model.dto.user
 * @ClassName UserUpdateRequset
 * @Version
 * @Description 用户更新 -- 管理员
 */

@SuppressWarnings("all")
@Data
public class UserUpdateRequestVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
