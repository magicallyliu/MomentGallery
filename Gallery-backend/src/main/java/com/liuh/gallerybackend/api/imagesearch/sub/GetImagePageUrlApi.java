package com.liuh.gallerybackend.api.imagesearch.sub;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author LiuH
 * @Date 2025/9/30 下午2:58
 * @PackageName com.liuh.gallerybackend.api.imagesearch.sub
 * @ClassName GetImagePageUrlApi
 * @Version
 * @Description 获取图片的页面url
 */

@SuppressWarnings("all")
@Slf4j
public class GetImagePageUrlApi {

    /**
     * 获取图片的页面url
     *
     * @param url
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
        //负载数据
        //image: http....
        //tn: pc
        //from: pc
        //image_source: PC_UPLOAD_URL
        //sdkParam: 一个地址

        //准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        //获取时间
        long uptime = System.currentTimeMillis();
        //请求地址
        //https://graph.baidu.com/upload?uptime=
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;

        try {
            //发送请求地址
            HttpResponse httpResponse = HttpRequest.post(url)
                    // 这里需要指定acs-token 不然会响应系统异常
                    .header("acs-token", RandomUtil.randomString(1))
                    .form(formData)
                    .timeout(5000)
                    .execute();

            //判断响应码
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片页面url失败");
            }
            ///解析响应
            //{"status: 0,  "message": "success", "data": {"url": "https://, "simg": 签名}
            String body = httpResponse.body();
            //将数据转换为map格式
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);

            //解析响应不存在, 或者响应的status不为0
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片页面url失败");
            }

            //获取Data数据
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            //获取url
            String dataUrl = (String) data.get("url");
            //因为url是直接获取, 会出现格式错误
            //使用hutool转换
            String searchResultUrl = URLUtil.decode(dataUrl, StandardCharsets.UTF_8);
            //如果该url为空
            if (ObjUtil.isNull(searchResultUrl)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效的url");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("获取图片页面url API失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

}
