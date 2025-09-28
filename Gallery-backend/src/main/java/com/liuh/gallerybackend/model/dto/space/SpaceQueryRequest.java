package com.liuh.gallerybackend.model.dto.space;

import com.liuh.gallerybackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/9/25 下午3:10
 * @PackageName com.liuh.gallerybackend.model.dto.space
 * @ClassName SpaceQueryRequest
 * @Version
 * @Description 查询空间请求
 */

@SuppressWarnings("all")
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

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

