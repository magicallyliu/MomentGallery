package com.liuh.gallerybackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liuh.gallerybackend.annotation.AuthCheck;
import com.liuh.gallerybackend.common.BaseResponse;
import com.liuh.gallerybackend.common.DeleteRequest;
import com.liuh.gallerybackend.common.ResultUtils;
import com.liuh.gallerybackend.constant.UserConstant;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUils;
import com.liuh.gallerybackend.model.dto.picture.PictureEditRequest;
import com.liuh.gallerybackend.model.dto.picture.PictureQueryRequest;
import com.liuh.gallerybackend.model.dto.picture.PictureUpdateRequest;
import com.liuh.gallerybackend.model.dto.space.*;
import com.liuh.gallerybackend.model.entity.Picture;
import com.liuh.gallerybackend.model.entity.Space;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.enums.PictureReviewStatusEnum;
import com.liuh.gallerybackend.model.enums.SpaceLevelEnum;
import com.liuh.gallerybackend.model.vo.PictureVO;
import com.liuh.gallerybackend.model.vo.SpaceVO;
import com.liuh.gallerybackend.service.PictureService;
import com.liuh.gallerybackend.service.SpaceService;
import com.liuh.gallerybackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author LiuH
 * @Date 2025/9/25 下午4:50
 * @PackageName com.liuh.gallerybackend.controller
 * @ClassName SpaceController
 * @Version
 * @Description
 */

@SuppressWarnings("all")
@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {
    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        //判断是否为空
        ThrowUils.throwIf(ObjUtil.isNull(spaceAddRequest), ErrorCode.PARAMS_ERROR);
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        long newId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(newId);
    }

    /**
     * 删除空间
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {

        //请求参数不存在, id不存在
        if (ObjUtil.isNull(deleteRequest) || deleteRequest.getId() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long spaceId = deleteRequest.getId();
        //判断空间是否存在
        Space oldSpace = spaceService.getById(spaceId);
        ThrowUils.throwIf(ObjUtil.isNull(oldSpace), ErrorCode.NOT_FOUND_ERROR);

        //只有本人和管理员可以操作
        //仅本人和管理员可以删除
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        //删除空间的同时, 删除图片
        //使用事务管理
        transactionTemplate.execute(status -> {
            boolean removeById = spaceService.removeById(spaceId);
            ThrowUils.throwIf(!removeById, ErrorCode.SYSTEM_ERROR, "删除失败");
            //查询该空间下的所有图片
            List<Picture> pictures = pictureService.list(new QueryWrapper<Picture>().eq("spaceId", spaceId));
            //获取该图片列表的id
            List<Long> oldPictureList = pictures.stream().map(Picture::getId).collect(Collectors.toList());
            //删除图片
            boolean remove = pictureService.removeByIds(oldPictureList);
            //删除桶内资源
            //删除存储桶的资源
            for (Picture picture : pictures) {
                pictureService.clearPictureFile(picture);
            }
            ThrowUils.throwIf(!remove, ErrorCode.SYSTEM_ERROR, "删除失败");
            return remove;

        });
        return ResultUtils.success(true);
    }

    /**
     * 更新空间信息 -- 面向管理员
     *
     * @param spaceUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest,
                                             HttpServletRequest request) {
        //判断需要修改的数据是否为空, 以及修改对象不能低于0
        if (ObjUtil.isNull(spaceUpdateRequest) || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断空间是否存在
        long id = spaceUpdateRequest.getId();
        Space byId = spaceService.getById(id);
        ThrowUils.throwIf(ObjUtil.isNull(byId), ErrorCode.NOT_FOUND_ERROR);

        //将图片信息提取出来
        //spaceUpdateRequest 中的数据 复制到 space (仅仅复制两者相同的字符)
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest, space);

        //填充数据
        spaceService.fillSpaceBySpaceLevel(space);

        //数据效验
        spaceService.validSpace(space, false);


        //操作数据库
        boolean b = spaceService.updateById(space);
        ThrowUils.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询空间信息, 面向管理员, 无须脱敏
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(Long id, HttpServletRequest request) {
        ThrowUils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Space byId = spaceService.getById(id);
        ThrowUils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(byId);
    }

    /**
     * 获取空间信息, 面向用户
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/VO")
    public BaseResponse<SpaceVO> getSpaceVOById(Long id, HttpServletRequest request) {
        ThrowUils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);


        //查询数据库
        Space oldSpace = spaceService.getById(id);
        ThrowUils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);

        //仅本人可以操作
        User loginUser = userService.getLoginUser(request);
        if (!oldSpace.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(spaceService.getSpaceVO(oldSpace, request));
    }

    /**
     * 分页获取空间列表（仅管理员可用）
     *
     * @param spaceQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        //参数不存在则不执行查询
        ThrowUils.throwIf(spaceQueryRequest == null,
                ErrorCode.PARAMS_ERROR, "分页查询参数错误");

        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    /**
     * 分页获取空间列表（封装类）
     *
     * @param spaceQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVoByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                         HttpServletRequest request) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUils.throwIf(size > 100, ErrorCode.PARAMS_ERROR);

        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
    }

    /**
     * 编辑空间（给用户使用） -- 修改空间名称
     *
     * @param spaceEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
//
        //填充数据 --  根据空间等级填充空间信息
        spaceService.fillSpaceBySpaceLevel(space);
        // 设置编辑时间
        space.setEditTime(new Date());
        // 数据校验
        spaceService.validSpace(space, false);
        User loginUser = userService.getLoginUser(request);

        // 判断是否存在
        long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取空间基本列表, 便于前端展示
     *
     * @return
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum ->
                        new SpaceLevel(
                                spaceLevelEnum.getValue(),
                                spaceLevelEnum.getText(),
                                spaceLevelEnum.getMaxSize(),
                                spaceLevelEnum.getMaxCount()
                        ))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }
}

