package com.liuh.gallerybackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liuh.gallerybackend.annotation.AuthCheck;
import com.liuh.gallerybackend.common.BaseResponse;
import com.liuh.gallerybackend.common.DeleteRequest;
import com.liuh.gallerybackend.common.ResultUtils;
import com.liuh.gallerybackend.constant.UserConstant;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUils;
import com.liuh.gallerybackend.model.dto.space.SpaceAddRequest;
import com.liuh.gallerybackend.model.dto.user.*;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.vo.LoginUserVO;
import com.liuh.gallerybackend.model.vo.UserVO;
import com.liuh.gallerybackend.service.SpaceService;
import com.liuh.gallerybackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/5/7 下午7:44
 * @PackageName com.liuh.gallerybackend.controller
 * @ClassName UserController
 * @Version
 * @Description 用户操作
 */

@SuppressWarnings("all")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        ThrowUils.throwIf(userRegisterRequest == null,
                ErrorCode.PARAMS_ERROR, "获取用户注册信息失败");

        String userPassword = userRegisterRequest.getUserPassword();
        String userAccount = userRegisterRequest.getUserAccount();
        String checkPassword = userRegisterRequest.getCheckPassword();

        long register = userService.userRegister(userAccount, userPassword, checkPassword);

        //在用户注册成功的时候, 同时创建私人空间
        User loginUser = userService.getLoginUser(request);
        spaceService.addSpace(new SpaceAddRequest(), loginUser);

        return ResultUtils.success(register);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUils.throwIf(userLoginRequest == null,
                ErrorCode.PARAMS_ERROR, "获取用户注册信息失败");

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();


        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     *
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        //获取登录数据并且进行脱敏
        LoginUserVO loginUserVO = userService.getLoginUserVO(userService.getLoginUser(request));
        return ResultUtils.success(loginUserVO);
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(request));
    }

    /**
     * 用户创建  --  面向管理员
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUils.throwIf(userAddRequest == null,
                ErrorCode.PARAMS_ERROR, "参数为空");

        //转换User
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);

        //设置默认值
        //设置默认密码 default password
        final String DEFAULT_PASSWORD = "12345678";
        //对密码进行加密
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        //添加
        user.setUserPassword(encryptPassword);

        // 如果未设置默认角色
        // 设置用户的默认角色
        if (user.getUserRole() == null) {
            user.setUserRole(UserConstant.DEFAULT_ROLE);
        }

        //插入数据
        boolean result = userService.save(user);
        //判断是否成功
        ThrowUils.throwIf(!result, ErrorCode.OPERATION_ERROR, "用户创建失败");
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据用户id获取用户  --  面向管理员
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserId(Long userId) {
        ThrowUils.throwIf(userId <= 0, ErrorCode.PARAMS_ERROR, "获取id无参数");

        //获取用户参数
        User user = userService.getById(userId);
        ThrowUils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ResultUtils.success(user);
    }

    /**
     * 根据用户id获取用户  --  面向普通用户
     */
    @PostMapping("/get/vo")
    public BaseResponse<UserVO> getUserIdVO(Long userId) {
        //获取未脱敏的数据
        BaseResponse<User> response = getUserId(userId);
        User user = response.getData();
        //需要返回脱敏数据
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户  --  面向管理员
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        //参数不存在 和 参数错误则不执行删除
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户删除参数错误");
        }

        //在数据库删除
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 用户更新  --  面向管理员
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        //参数不存在 和 参数错误则不执行删除
        if (userUpdateRequest == null || userUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户更新参数错误");
        }

        //转化为用户表
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);

        //在数据库更新
        boolean b = userService.updateById(user);
        return ResultUtils.success(b);
    }

    /**
     * 分页查询(脱敏的数据) --  面向管理员
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserByPageVO(@RequestBody UserQueryRequest userQueryRequest) {
        //参数不存在则不执行查询
        ThrowUils.throwIf(userQueryRequest == null,
                ErrorCode.PARAMS_ERROR, "分页查询参数错误");

        //获取查询限制 -- 页号, 单页数量
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();

        //查询
        //参数1--页面设置, 参数2--查询条件设置
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));

        //脱敏
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        //将脱敏后的数据设置到分页表中
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}
