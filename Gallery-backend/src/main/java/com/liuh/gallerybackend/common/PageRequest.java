package com.liuh.gallerybackend.common;

import lombok.Data;

/**
 * @Author LiuH
 * @Date 2025/4/24 下午10:14
 * @PackageName com.liuh.gallerybackend.common
 * @ClassName PageRequest
 * @Version
 * @Description 分页请求包装类，接受页号、页面大小、排序字段、排序顶序参数：
 */

@SuppressWarnings("all")
@Data
public class PageRequest {

    public static final String DESC = "descend";
    /**
     * 当前页号
     */
    private int current;

    /**
     * 页面存放的数据数量
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式(默认升序)
     * 设置为降序
     */
    private String sortOrder = DESC;
}
