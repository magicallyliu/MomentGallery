package com.liuh.gallerybackend.mananger.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/10/7 下午9:12
 * @PackageName com.liuh.gallerybackend.mananger.auth.model
 * @ClassName SpaceUserAuthConfig
 * @Version
 * @Description 权限配置 -- 空间成员
 */

@SuppressWarnings("all")
@Data
public class SpaceUserAuthConfig implements Serializable {

    /**
     * 权限列表
     */
    private List<SpaceUserPermission> permissions;

    /**
     * 角色列表
     */
    private List<SpaceUserRole> roles;

    private static final long serialVersionUID = 1L;
}
