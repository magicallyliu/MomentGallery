package com.liuh.gallerybackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/7/21 上午2:31
 * @PackageName com.liuh.gallerybackend.model.dto.pivture
 * @ClassName PictureUploadRequest
 * @Version
 * @Description 图片的唯一标识
 */

@SuppressWarnings("all")
@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片id,  图片的唯一辨识
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
