package com.liuh.gallerybackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/10/5 下午3:43
 * @PackageName com.liuh.gallerybackend.model.dto.picture
 * @ClassName PictureEditByBatchRequest
 * @Version
 * @Description 用于批量修改图片信息
 */

@SuppressWarnings("all")
@Data
public class PictureEditByBatchRequest implements Serializable {

    /**
     * 图片 id 列表
     */
    private List<Long> pictureIdList;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 命名规则
     */
    private String nameRule;

    private static final long serialVersionUID = 1L;
}

