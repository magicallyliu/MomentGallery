package com.liuh.gallerybackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/10/4 下午7:36
 * @PackageName com.liuh.gallerybackend.model.dto.picture
 * @ClassName SearchPictureByColorRequest
 * @Version
 * @Description 以颜色搜图请求
 */

@SuppressWarnings("all")
@Data
public class SearchPictureByColorRequest implements Serializable {

    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 空间 id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
