package com.liuh.gallerybackend.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/10/6 下午8:27
 * @PackageName com.liuh.gallerybackend.model.vo.space.analyze
 * @ClassName SpaceTagAnalyzeResponse
 * @Version
 * @Description  空间图片标签分析响应
 */

@SuppressWarnings("all")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceTagAnalyzeResponse implements Serializable {

    /**
     * 标签名称
     */
    private String tag;

    /**
     * 使用次数
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
