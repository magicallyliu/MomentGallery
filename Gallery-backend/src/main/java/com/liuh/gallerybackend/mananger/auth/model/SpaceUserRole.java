package com.liuh.gallerybackend.mananger.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/10/7 下午9:15
 * @PackageName com.liuh.gallerybackend.mananger.auth.model
 * @ClassName SpaceUserRole
 * @Version
 * @Description 空间权限的角色列表
 */

@SuppressWarnings("all")
@Data
public class SpaceUserRole implements Serializable {

    /**
     * 角色键
     */
    private String key;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 权限键列表
     */
    private List<String> permissions;

    /**
     * 角色描述
     */
    private String description;

    private static final long serialVersionUID = 1L;
}
