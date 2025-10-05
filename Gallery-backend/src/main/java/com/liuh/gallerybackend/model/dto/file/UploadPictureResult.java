package com.liuh.gallerybackend.model.dto.file;

import lombok.Data;

/**
 * @Author LiuH
 * @Date 2025/7/20 上午3:02
 * @PackageName com.liuh.gallerybackend.model.dto.file
 * @ClassName UploadPictureResult
 * @Version
 * @Description 上传文件的结果 -- 文件的各个信息
 */

@SuppressWarnings("all")
@Data
public class UploadPictureResult {

    /**
     * 图片地址
     */
    private String url;
    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;

    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 图片主色调
     */
    private String picColor;
}
