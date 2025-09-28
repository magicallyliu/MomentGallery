package com.liuh.gallerybackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/9/25 下午3:07
 * @PackageName com.liuh.gallerybackend.model.dto.space
 * @ClassName SpaceEditRequest
 * @Version
 * @Description 空间编辑请求 -- 仅允许编辑空间名称
 */

@SuppressWarnings("all")
@Data
public class SpaceEditRequest implements Serializable {

    /**
     * 空间 id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    private static final long serialVersionUID = 1L;
}
