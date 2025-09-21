package com.liuh.gallerybackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/9/19 下午4:28
 * @PackageName com.liuh.gallerybackend.model.dto.picture
 * @ClassName PictureReviewRequest
 * @Version
 * @Description 用于图片审核请求
 */

@SuppressWarnings("all")
@Data
public class PictureReviewRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

}
