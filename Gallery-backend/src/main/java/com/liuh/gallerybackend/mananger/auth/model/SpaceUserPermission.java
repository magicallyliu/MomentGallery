package com.liuh.gallerybackend.mananger.auth.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/10/7 下午9:14
 * @PackageName com.liuh.gallerybackend.mananger.auth.model
 * @ClassName SpaceUserPermission
 * @Version
 * @Description 空间权限的权限列表
 */

@SuppressWarnings("all")
@Data
public class SpaceUserPermission implements Serializable {

    /**
     * 权限键
     */
    private String key;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限描述
     */
    private String description;

    private static final long serialVersionUID = 1L;

}
