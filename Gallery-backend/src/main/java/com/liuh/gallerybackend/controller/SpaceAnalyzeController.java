package com.liuh.gallerybackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.liuh.gallerybackend.common.BaseResponse;
import com.liuh.gallerybackend.common.ResultUtils;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUtils;
import com.liuh.gallerybackend.model.dto.space.analyze.*;
import com.liuh.gallerybackend.model.entity.Space;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.vo.space.analyze.*;
import com.liuh.gallerybackend.service.SpaceAnalyzeService;
import com.liuh.gallerybackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/10/6 下午7:24
 * @PackageName com.liuh.gallerybackend.controller
 * @ClassName SpaceAnalyzeController
 * @Version
 * @Description
 */

@SuppressWarnings("all")
@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {

    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    @Resource
    private UserService userService;

    /**
     * 获取空间的使用情况 返回使用的大小和数量
     *
     * @param spaceUsageAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyze = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyze);
    }

    /**
     * 获取空间分类分析
     * 按照空间分类来进行分组
     * 统计每组的图片数量和大小
     *
     * @param spaceCategoryAnalyzeResponse
     * @param request
     * @return
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceCategoryAnzlyzeRequest SpaceCategoryAnzlyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(SpaceCategoryAnzlyzeRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyzeResponses = spaceAnalyzeService.getSpaceCategoryAnalyze(SpaceCategoryAnzlyzeRequest, loginUser);
        return ResultUtils.success(spaceCategoryAnalyzeResponses);
    }

    /**
     * 获取空间图片标签分析
     * 返回每一个标签的使用次数
     *
     * @param SpaceTagAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }


    /**
     * 获取空间图片大小分析
     * 更具需要查询图片大小范围 , 返回其图片的数量
     *
     * @param spaceSizeAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceSizeAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

    /**
     * 用户行为分析
     * 返回用户一段时间内的上传数量
     * 可针对一个用户返回
     *
     * @param spaceUserAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceUserAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

    /**
     *  获取空间使用排行(管理员)
     * @param spaceRankAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/rank")
    public BaseResponse<List<Space>> getSpaceRankAnalyze(@RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceRankAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<Space> resultList = spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

}
