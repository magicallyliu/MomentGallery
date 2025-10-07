package com.liuh.gallerybackend.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author LiuH
 * @Date 2025/10/7 下午2:24
 * @PackageName com.liuh.gallerybackend.model.dto.space.analyze
 * @ClassName SpaceUserAnalyzeRequest
 * @Version
 * @Description 用户上传行为分析 需要有时间维度, 可对单个用分析
 */

@SuppressWarnings("all")
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 时间维度：day / week / month
     */
    private String timeDimension;
}
