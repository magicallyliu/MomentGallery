package com.liuh.gallerybackend.controller;

import com.liuh.gallerybackend.annotation.AuthCheck;
import com.liuh.gallerybackend.common.BaseResponse;
import com.liuh.gallerybackend.common.ResultUtils;
import com.liuh.gallerybackend.constant.UserConstant;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.mananger.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @Author LiuH
 * @Date 2025/6/5 下午8:34
 * @PackageName com.liuh.gallerybackend.controller
 * @ClassName FileController
 * @Version
 * @Description 文件上传和下载
 */

@SuppressWarnings("all")
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 接收文件, 确定文件上传位置
     * 创建临时文件
     * 上传文件
     * 删除临时文件
     *
     * @param multipartFile 文件上传的接口
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")

    public BaseResponse<String> testUploadFile(@RequestParam("file") MultipartFile multipartFile) {
        //文件名称
        String filename = multipartFile.getOriginalFilename();
        //上传的位置
        String filepath = String.format("/test/%s", filename);

        //创建临时文件
        File file = null;

        //指定路劲, 无后缀
        try {
            file = File.createTempFile(filepath, null);
            //将文件转换到本地;临时文件
            multipartFile.transferTo(file);
            //上传文件
            cosManager.putObject(filepath, file);

            //返回可访问 的文件的地址
            return ResultUtils.success(filepath);
        } catch (IOException e) {
            log.error("file upload error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            //删除临时文件
            if (file != null) {
                file.delete();
                boolean deleted = file.delete();
                if (!deleted) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 测试文件下载
     *
     * @param filepath 文件下载的位置
     * @param response 用于控制接口的返回
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        //创建流, 需要关闭
        COSObjectInputStream objectContent = null;

        try {
            COSObject object = cosManager.getObject(filepath);
            //获得数据流
            objectContent = object.getObjectContent();
            //将获取到的流转换为字节
            byte[] byteArray = IOUtils.toByteArray(objectContent);

            //下载 到前端
            // 设置响应头
            //用于确认是 下载还是查看文件
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            //写入相应响应头
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();//刷新
        } catch (Exception e) {
            log.error("file upload error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件下载失败");
        } finally {
            //关闭流
            if (objectContent != null) {
                objectContent.close();
            }
        }


    }
}
















