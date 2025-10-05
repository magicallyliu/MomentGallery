package com.liuh.gallerybackend.api.imagesearch.model;

import lombok.Data;

/**
 * @Author LiuH
 * @Date 2025/9/30 下午2:54
 * @PackageName com.liuh.gallerybackend.api.imagesearch.model
 * @ClassName ImageSearchResult
 * @Version
 * @Description 用于以图搜图返回的结果
 */

@SuppressWarnings("all")
@Data
public class ImageSearchResult {

    /**
     * 缩略图
     */
    private String thumbUrl;

    /**
     * 图片源地址
     */
    private String fromUrl;
}
