package com.liuh.gallerybackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/10/6 下午5:13
 * @PackageName com.liuh.gallerybackend.model.dto.space.analyze
 * @ClassName SpaceAnalyzeRequest
 * @Version
 * @Description 通用空间分析请求
 * 管理员全空间分析
 * 管理员公共图库分析
 * 用户私人空间分析
 */

@SuppressWarnings("all")
@Data
public class SpaceAnalyzeRequest implements Serializable {

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 是否查询公共图库
     */
    private boolean queryPublic;

    /**
     * 全空间分析
     */
    private boolean queryAll;

    private static final long serialVersionUID = 1L;
}
