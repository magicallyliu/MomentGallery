package com.liuh.gallerybackend.api.imagesearch;

import com.liuh.gallerybackend.api.imagesearch.model.ImageSearchResult;
import com.liuh.gallerybackend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.liuh.gallerybackend.api.imagesearch.sub.GetImageListApi;
import com.liuh.gallerybackend.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/10/2 下午7:37
 * @PackageName com.liuh.gallerybackend.api.imagesearch
 * @ClassName imageSearchApiFacade
 * @Version
 * @Description 门面模式, 用于调用图片搜索接口
 */

@SuppressWarnings("all")
@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片 -- 以图搜图
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        //获取图片的页面url
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        //获取图片列表接口
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        //获取图片列表
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }


}


