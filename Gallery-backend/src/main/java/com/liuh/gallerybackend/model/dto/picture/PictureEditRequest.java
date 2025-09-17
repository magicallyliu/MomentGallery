package com.liuh.gallerybackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/9/9 下午10:12
 * @PackageName com.liuh.gallerybackend.model.dto.picture
 * @ClassName PictureEditRequest
 * @Version
 * @Description 图片修改请求
 */

@SuppressWarnings("all")
@Data
public class PictureEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
