package com.liuh.gallerybackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/10/7 下午2:42
 * @PackageName com.liuh.gallerybackend.model.dto.space.analyze
 * @ClassName SpaceRankAnalyzeRequest
 * @Version
 * @Description 管理员操作, 对用户使用空间使用排行分析
 */

@SuppressWarnings("all")
@Data
public class SpaceRankAnalyzeRequest implements Serializable {

    /**
     * 排名前 N 的空间
     */
    private Integer topN = 10;

    private static final long serialVersionUID = 1L;
}

