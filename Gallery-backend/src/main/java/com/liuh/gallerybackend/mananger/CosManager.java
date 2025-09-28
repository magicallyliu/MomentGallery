package com.liuh.gallerybackend.mananger;

import cn.hutool.core.io.FileUtil;
import com.liuh.gallerybackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/6/5 下午8:27
 * @PackageName com.liuh.gallerybackend.mananger
 * @ClassName CosManager
 * @Version
 * @Description 通用的文件上传和下载
 */

@SuppressWarnings("all")
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键  保存的文件位置
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 用于将对象从服务器中取出
     *
     * @param key 路径
     * @return
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传图片对象(附带文件的解析信息)
     *
     * @param key  唯一键  保存的文件位置
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);

        //对文件进行处理 -- 获取图片信息
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        //参考: https://cloud.tencent.com/document/product/436/55377
        picOperations.setIsPicInfo(1);


        //图片处理规则
        List<PicOperations.Rule> rules = new ArrayList<>();
        //图片压缩(转换为webp 格式)
        //去除文件地址后缀
        // https://moment-gallery-1353804205.cos.ap-guangzhou.myqcloud.com//public/1920102552364191745/2025-09-21_BRv2GNdMFeOo3GvN.
        //加上webp后缀, 形成新格式
        String webpKey = FileUtil.mainName(key) + ".webp";
        //根据以下规则构建
        //"is_pic_info": 1,
        //  "rules": [{
        //      "fileid": "exampleobject",
        //      "rule": "imageMogr2/format/<Format>"
        //
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setRule("imageMogr2/format/webp");
        //需要修改的桶
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setFileId(webpKey);
        //将新规则添加到集合中, 方便添加
        rules.add(compressRule);


        //缩略图处理 , 仅对 > 20kb 的图片生成缩略图
        if (file.length() > 2 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            //缩略图的地址 文件不带后缀名的地址 +  _thumbnail. + 文件后缀名
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            ///thumbnail/<Width>x<Height>>(如果大于原宽高, 则不处理)
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s", 256, 256));
            //需要修改的桶
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            //将新规则添加到集合中, 方便添加
            rules.add(thumbnailRule);
        }

        //将规则集合添加到处理规则中
        picOperations.setRules(rules);
        //构造处理参数
        putObjectRequest.setPicOperations(picOperations);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
//        //删除原始图片\
//        this.deleteObject(cosObjectKey(key));
        return putObjectResult;
    }

    /**
     * 删除对象
     *
     * @param key 文件 key
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

    /**
     * 将一个url转换为一个合法腾讯云sos对象键
     * @param url
     * @return
     */
    public String cosObjectKey(String url) {
        if (url != null) {
            try {
                URI uri = new URI(url);
                String path = uri.getPath();
                // 替换非法字符为下划线
                path = path.replaceAll("[\\?%#&+=]", "_");
                // 确保不以斜杠开头（可选）
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                url = path;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return url;
    }
}