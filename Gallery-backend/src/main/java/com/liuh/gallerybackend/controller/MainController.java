package com.liuh.gallerybackend.controller;

import com.liuh.gallerybackend.common.BaseResponse;
import com.liuh.gallerybackend.common.ResultUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author LiuH
 * @Date 2025/4/24 下午11:20
 * @PackageName com.liuh.gallerybackend.controller
 * @ClassName MainController
 * @Version
 * @Description 健康检查
 */

@SuppressWarnings("all")
@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查
     * @return
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("完成健康检查");
    }
}
