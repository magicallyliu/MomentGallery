package com.liuh.gallerybackend.mananger.auth;

import com.liuh.gallerybackend.model.entity.Picture;
import com.liuh.gallerybackend.model.entity.Space;
import com.liuh.gallerybackend.model.entity.SpaceUser;
import lombok.Data;

/**
 * @Author LiuH
 * @Date 2025/10/9 下午9:09
 * @PackageName com.liuh.gallerybackend.mananger.auth
 * @ClassName SpaceUserAuthContext
 * @Version
 * @Description 表示用户在特定空间内的授权上下文，包括关联的图片、空间和用户信息。
 */

@SuppressWarnings("all")
@Data
public class SpaceUserAuthContext {

    /**
     * 临时参数，不同请求对应的 id 可能不同
     */
    private Long id;

    /**
     * 图片 ID
     */
    private Long pictureId;

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 空间用户 space_user ID
     */
    private Long spaceUserId;

    /**
     * 图片信息
     */
    private Picture picture;

    /**
     * 空间信息
     */
    private Space space;

    /**
     * 空间用户信息
     */
    private SpaceUser spaceUser;
}
