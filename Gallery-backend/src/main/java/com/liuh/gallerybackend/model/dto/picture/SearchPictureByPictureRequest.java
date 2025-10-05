package com.liuh.gallerybackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/10/2 下午7:44
 * @PackageName com.liuh.gallerybackend.model.dto.picture
 * @ClassName SearchPictureByPictureRequest
 * @Version
 * @Description 以图搜图请求
 */

@SuppressWarnings("all")
@Data
public class SearchPictureByPictureRequest implements Serializable {

    /**
     * 图片id
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
