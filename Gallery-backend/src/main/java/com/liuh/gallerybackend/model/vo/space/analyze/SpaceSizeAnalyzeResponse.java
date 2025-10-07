package com.liuh.gallerybackend.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/10/7 下午2:12
 * @PackageName com.liuh.gallerybackend.model.vo.space.analyze
 * @ClassName SpaceSizeAnalyzeRespone
 * @Version
 * @Description 空间图片大小分析响应
 */

@SuppressWarnings("all")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceSizeAnalyzeResponse implements Serializable {

    /**
     * 图片大小范围
     */
    private String sizeRange;

    /**
     * 图片数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
