package com.liuh.gallerybackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/9/25 下午3:06
 * @PackageName com.liuh.gallerybackend.model.dto.space
 * @ClassName SpaceAddRequest
 * @Version
 * @Description 空间创建请求
 */

@SuppressWarnings("all")
@Data
public class SpaceAddRequest implements Serializable {

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    private static final long serialVersionUID = 1L;
}
