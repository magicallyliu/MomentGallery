package com.liuh.gallerybackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/9/21 上午10:19
 * @PackageName com.liuh.gallerybackend.model.dto.picture
 * @ClassName PictureUploadByBatchRequest
 * @Version
 * @Description 用于批量抓取图片的请求
 */

@SuppressWarnings("all")
@Data
public class PictureUploadByBatchRequest implements Serializable {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count;

    /**
     * 图片名称前缀
     */
    private String namePrefix;

    private static final long serialVersionUID = 1L;
}
