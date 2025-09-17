package com.liuh.gallerybackend.mananger;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
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
 */

@SuppressWarnings("all")
@Slf4j
@Service
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
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
     * 效验图片
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
