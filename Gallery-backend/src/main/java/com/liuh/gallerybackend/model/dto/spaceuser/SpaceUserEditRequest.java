package com.liuh.gallerybackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/10/7 下午7:57
 * @PackageName com.liuh.gallerybackend.model.dto.spaceuser
 * @ClassName SpaceUserEditRequest
 * @Version
 * @Description 编辑空间请求 -- 修改用户在空间的角色
 */

@SuppressWarnings("all")
@Data
public class SpaceUserEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}

