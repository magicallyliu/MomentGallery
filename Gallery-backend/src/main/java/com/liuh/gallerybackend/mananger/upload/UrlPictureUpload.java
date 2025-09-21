package com.liuh.gallerybackend.mananger.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/9/20 下午12:51
 * @PackageName com.liuh.gallerybackend.mananger.upload
 * @ClassName UrlPictureUploadImpl
 * @Version
 * @Description 使用Url上传图片 -- 模版实现之子类
 */

@SuppressWarnings("all")
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
  @Override
  protected void validPicture(Object inputSource) {
    String fileUrl = (String) inputSource;
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
      execute = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
      //未正常返回, 无需执行其他代码
      //如不支持HEAD的请求
      if (execute == null || !execute.equals(HttpStatus.HTTP_OK)) {
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

  @Override
  protected String getOriginFilename(Object inputSource) {
    String fileUrl = (String) inputSource;
    //获取文件名并返回
    return FileUtil.mainName(fileUrl);
  }

  @Override
  protected void processFile(Object inputSource, File file) throws Exception {
    //下载文件到临时目录
    String fileUrl = (String) inputSource;
    HttpUtil.downloadFile(fileUrl, file);
  }
}
