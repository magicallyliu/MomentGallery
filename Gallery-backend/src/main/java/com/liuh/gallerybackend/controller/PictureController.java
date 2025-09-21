package com.liuh.gallerybackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liuh.gallerybackend.annotation.AuthCheck;
import com.liuh.gallerybackend.common.BaseResponse;
import com.liuh.gallerybackend.common.DeleteRequest;
import com.liuh.gallerybackend.common.ResultUtils;
import com.liuh.gallerybackend.constant.UserConstant;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUils;
import com.liuh.gallerybackend.model.dto.picture.*;
import com.liuh.gallerybackend.model.entity.Picture;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.enums.PictureReviewStatusEnum;
import com.liuh.gallerybackend.model.vo.PictureTagCategory;
import com.liuh.gallerybackend.model.vo.PictureVO;
import com.liuh.gallerybackend.service.PictureService;
import com.liuh.gallerybackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/6/5 下午8:34
 * @PackageName com.liuh.gallerybackend.controller
 * @ClassName pictureController
 * @Version
 * @Description 文件上传和下载
 */

@SuppressWarnings("all")
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    /**
     * 上传图片
     *
     * @param file                 上传的文件
     * @param pictureUploadRequest
     * @param response
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPictureByUrl
    (@RequestBody PictureUploadRequest pictureUploadRequest,
     HttpServletRequest request) {
        //获取当前用户登录的信息
        User loginUser = userService.getLoginUser(request);
        //得到文件的地址
        String fileUrl = pictureUploadRequest.getFileUrl();
        //上传图片
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过url上传图片
     * @param multipartFile
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/url")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture
            (@RequestParam("multipartFile") MultipartFile multipartFile,
             PictureUploadRequest pictureUploadRequest,
             HttpServletRequest request) {
        //获取当前用户登录的信息
        User loginUser = userService.getLoginUser(request);
        //上传图片
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        //请求删除的参数为空, 或者id不存在
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //判断权限, 本人或者管理员可以删除
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        //已经存在的图片
        Picture byId = pictureService.getById(id);
        //判断图片是否存在
        ThrowUils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR);

        //仅本人和管理员可以删除
        if (!byId.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        //在数据库中删除该图片
        boolean removeById = pictureService.removeById(id);
        ThrowUils.throwIf(!removeById, ErrorCode.OPERATION_ERROR, "删除失败");
        return ResultUtils.success(true);
    }

    /**
     * 修改数据
     *
     * @param pictureUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
                                               HttpServletRequest request) {
        //判断需要修改的数据是否为空, 以及修改对象不能低于0
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //将图片信息提取出来
        Picture picture = new Picture();
        //将pictureUpdateRequest 中的数据 复制到 picture 中去 (仅仅复制两者相同的字符)
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        //JSONUtil.toJsonStr可以将任意对象（Bean、Map、集合等）直接转换为JSON字符串。
        // 如果对象是有序的Map等对象，则转换后的JSON字符串也是有序的
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));

        //数据效验
        pictureService.validPicture(picture);

        //判断数据
        Long id = pictureUpdateRequest.getId();
        Picture byId = pictureService.getById(id);
        ThrowUils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR);
        //获取用户信息
        User loginUser = userService.getLoginUser(request);
        //添加审核参数
        pictureService.fillReviewParams(picture, loginUser);

        //操作数据库
        boolean b = pictureService.updateById(picture);
        ThrowUils.throwIf(b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询图片, 面向管理员, 无须脱敏
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(Long id, HttpServletRequest request) {
        ThrowUils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Picture byId = pictureService.getById(id);
        ThrowUils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(byId);

    }

    /**
     * 获取图片, 面向用户
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/VO")
    public BaseResponse<PictureVO> getPictureVOById(Long id, HttpServletRequest request) {
        ThrowUils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Picture byId = pictureService.getById(id);
        ThrowUils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(pictureService.getPictureVO(byId, request));
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     *
     * @param pictureQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        //参数不存在则不执行查询
        ThrowUils.throwIf(pictureQueryRequest == null,
                ErrorCode.PARAMS_ERROR, "分页查询参数错误");

        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVoByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUils.throwIf(size > 100, ErrorCode.PARAMS_ERROR);
        //普通用户默认只能看到审核通过的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 编辑图片（给用户使用）
     *
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        pictureService.validPicture(picture);
        User loginUser = userService.getLoginUser(request);
        //添加审核参数
        pictureService.fillReviewParams(picture, loginUser);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 展示展示的热门标签
     *
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("测试", "热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("测试", "模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


    /**
     * 审核图片
     *
     * @param pictureReviewRequest 审核图片的数据
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        //参数不存在则不执行查询
        ThrowUils.throwIf(pictureReviewRequest == null,
                ErrorCode.PARAMS_ERROR, "审核图片参数错误");

        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量抓取并获取图片
     * @param pictureUploadByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest ,
                                                      HttpServletRequest request) {
        //参数不存在则不执行查询
        ThrowUils.throwIf(pictureUploadByBatchRequest == null,
                ErrorCode.PARAMS_ERROR, "审核图片参数错误");

        User loginUser = userService.getLoginUser(request);
        Integer integer = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(integer);
    }

}
















