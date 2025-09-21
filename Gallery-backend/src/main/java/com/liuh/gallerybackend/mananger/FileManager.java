package com.liuh.gallerybackend.mananger;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.liuh.gallerybackend.common.ResultUtils;
import com.liuh.gallerybackend.config.CosClientConfig;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUils;
import com.liuh.gallerybackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/6/5 下午8:27
 * @PackageName com.liuh.gallerybackend.mananger
 * @ClassName FileManager
 * @Version
 * @Description 提供一个上传图片并返回图片解析的方法
 * 已经弃用,  现在使用的是PictureUploadTemplate模版方法
 */

@SuppressWarnings("all")
@Slf4j
@Service
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 直接上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        //效验图片
        validPicture(multipartFile);

        //图片上传地址
        //随机生成一个16位地址, 防止地址重复
        String randomFile = RandomUtil.randomString(16);
        //得到文件名
        String originalFilename = multipartFile.getOriginalFilename();
        //文件路径 上传文件的时间 + 随机地址 + 文件名
        String uploadFileName = String.format("%s_%s_%s",
                DateUtil.formatDate(new Date()), randomFile, originalFilename);
        //最终用户存放位置
        String filepath = String.format("%s/%s", uploadPathPrefix, uploadFileName);

        //解析结果并返回
        //创建临时文件
        File file = null;
        //指定路劲, 无后缀
        try {
            file = File.createTempFile(filepath, null);
            //将文件转换到本地;临时文件
            multipartFile.transferTo(file);
            //上传文件
            //获取上传结果对象
            PutObjectResult putObjectResult = cosManager.putPictureObject(filepath, file);
            //获取原始信息的图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //封装返回结果
            //计算宽高比
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();

            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + filepath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());

            //返回可访问 的文件的地址
            return uploadPictureResult;
        } catch (IOException e) {
            log.error("file upload error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            //清除临时文件
            deleteTempFile(file);
        }

    }

    /**
     * 通过URL来上传图片
     *
     * @param fileUrl          文件地址
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {
        //效验图片
//        validPicture(multipartFile);
        //更改为通过url
        validPicture(fileUrl);

        //图片上传地址
        //随机生成一个16位地址, 防止地址重复
        String randomFile = RandomUtil.randomString(16);
        //得到文件名
//        String originalFilename = multipartFile.getOriginalFilename();
        //去除url的后缀
        String originalFilename = FileUtil.mainName(fileUrl);

        //文件路径 上传文件的时间 + 随机地址 + 文件名
        String uploadFileName = String.format("%s_%s_%s",
                DateUtil.formatDate(new Date()), randomFile, originalFilename);
        //最终用户存放位置
        String filepath = String.format("%s/%s", uploadPathPrefix, uploadFileName);

        //解析结果并返回
        //创建临时文件
        File file = null;
        //指定路劲, 无后缀
        try {
            file = File.createTempFile(filepath, null);
            //将文件转换到本地;临时文件
//            multipartFile.transferTo(file);
            //下载文件
            HttpUtil.downloadFile(fileUrl, file);

            //上传文件
            //获取上传结果对象
            PutObjectResult putObjectResult = cosManager.putPictureObject(filepath, file);
            //获取原始信息的图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //封装返回结果
            //计算宽高比
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();

            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + filepath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());

            //返回可访问 的文件的地址
            return uploadPictureResult;
        } catch (IOException e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            //清除临时文件
            deleteTempFile(file);
        }

    }


    /**
     * 删除临时文件
     *
     * @param file
     */
    public static void deleteTempFile(File file) {
        //删除临时文件
        if (file != null) {

            boolean deleted = file.delete();
            if (!deleted) {
                log.error("file delete error, filepath = {}", file.getAbsolutePath());
            }
        }
    }

    /**
     * 效验图片 -- 传入url
     *
     * @param fileUrl
     */
    private void validPicture(String fileUrl) {
        //1. 效验非空
        ThrowUils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址为空");

        //2. 效验 URL 格式
        //通过java自带的url 来辅助效验
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        //3. 效验 URL 协议
        //效验前缀
        ThrowUils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件");

        //4. 发送 HEAD 请求验证文件信息是否存在
        HttpResponse execute = null;

        try {
            HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            //未正常返回, 无需执行其他代码
            //如不支持HEAD的请求
            if (!execute.equals(HttpStatus.HTTP_OK)) {
                return;
            }

            //5. 文件存在, 文件类型效验
            //取出头信息
            String contentType = execute.header("Content-Type");
            //不为空, 才效验是否合法
            if (StrUtil.isNotBlank(contentType)){
                if (StrUtil.isNotBlank(contentType)) {
                    // 允许的图片类型
                    final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                    ThrowUils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                            ErrorCode.PARAMS_ERROR, "文件类型错误");
                }
            }

            //6. 文件存在, 文件大小效验
            String contentLength = execute.header("Content-Length");
            if(StrUtil.isNotBlank(contentLength)) {
                try {
                    //把字符串转换为Long类型
                    long size = Long.parseLong(contentLength);
                    final long ONE_M = 1024 * 1024;
                    ThrowUils.throwIf(size > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过 2MB");

                } catch (NumberFormatException e) {
                    //文件大小格式异常
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        }finally {
            //释放资源
            if(execute != null){
                execute.close();
            }
        }
    }

    /**
     * 效验图片 -- 直接传入文件
     *
     * @param multipartFile 文件
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        //1. 效验文件大小
        long size = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUils.throwIf(size > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过 2MB");

        //2. 效验文件后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //允许上传的文件后缀集合
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "png", "gif", "jpg", "jpe", "webp");
        ThrowUils.throwIf(!ALLOW_FORMAT_LIST.contains(suffix),
                ErrorCode.PARAMS_ERROR, "上传文件类型错误");
    }


}
