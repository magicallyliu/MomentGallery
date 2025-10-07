package com.liuh.gallerybackend.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/10/7 下午2:26
 * @PackageName com.liuh.gallerybackend.model.vo.space.analyze
 * @ClassName SpaceUserAnalyzeResponse
 * @Version
 * @Description  用户行为分析, 需要返回时间区间内上传图片数量
 */

@SuppressWarnings("all")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceUserAnalyzeResponse implements Serializable {

    /**
     * 时间区间
     */
    private String period;

    /**
     * 上传数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
