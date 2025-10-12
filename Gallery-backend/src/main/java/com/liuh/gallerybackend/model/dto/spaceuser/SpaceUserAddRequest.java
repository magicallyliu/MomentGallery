package com.liuh.gallerybackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/10/7 下午7:54
 * @PackageName com.liuh.gallerybackend.model.dto.spaceuser
 * @ClassName SpaceUserAddRequest
 * @Version
 * @Description 创建空间成员请求类
 */

@SuppressWarnings("all")
@Data
public class SpaceUserAddRequest implements Serializable {

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}

