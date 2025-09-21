package com.liuh.gallerybackend.mananger.upload;

import cn.hutool.core.io.FileUtil;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/9/20 下午12:44
 * @PackageName com.liuh.gallerybackend.mananger.upload
 * @ClassName FilePictureUploadImpl
 * @Version
 * @Description 文件直接上传 -- 模版实现之子类
 */

@SuppressWarnings("all")
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
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

    @Override
    protected String getOriginFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
