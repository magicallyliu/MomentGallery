package com.liuh.gallerybackend.model.vo;

import com.liuh.gallerybackend.model.entity.Picture;
import lombok.Data;

import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/9/17 上午9:10
 * @PackageName com.liuh.gallerybackend.model.dto.picture
 * @ClassName PictureTagCategory
 * @Version
 * @Description 暂时展示页面标签列表
 */

@SuppressWarnings("all")
@Data
public class PictureTagCategory {

    /**
     * 标签列表
     */
    private List<String> TagList;

    /**
     * 分类列表
     */
    private List<String> categoryList; ;
}
