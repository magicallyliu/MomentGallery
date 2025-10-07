package com.liuh.gallerybackend.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/10/6 下午7:59
 * @PackageName com.liuh.gallerybackend.model.vo.space.analyze
 * @ClassName SpaceCategoryAnalyzeResponse
 * @Version
 * @Description 空间图片分类分析响应
 */

@SuppressWarnings("all")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceCategoryAnalyzeResponse implements Serializable {

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片数量
     */
    private Long count;

    /**
     * 分类图片总大小
     */
    private Long totalSize;

    private static final long serialVersionUID = 1L;
}
