package com.liuh.gallerybackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/9/25 下午3:09
 * @PackageName com.liuh.gallerybackend.model.dto.space
 * @ClassName SpaceUpdateRequest
 * @Version
 * @Description 空间更新请求 -- 面向管理员
 */

@SuppressWarnings("all")
@Data
public class SpaceUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    private static final long serialVersionUID = 1L;
}
