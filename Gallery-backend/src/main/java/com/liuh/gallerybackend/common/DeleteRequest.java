package com.liuh.gallerybackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/4/24 下午10:39
 * @PackageName com.liuh.gallerybackend.common
 * @ClassName DeleteRequest
 * @Version
 * @Description 通用的删除请求类
 */

@SuppressWarnings("all")
@Data
public class DeleteRequest implements Serializable {

    /**
     * 需要删除数据的id
     */
    private long id;

    /**
     * 序列版本
     */
    private static final long serialVersionUID = 1l;
}
