package com.liuh.gallerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liuh.gallerybackend.model.dto.user.UserQueryRequest;
import com.liuh.gallerybackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liuh.gallerybackend.model.vo.LoginUserVO;
import com.liuh.gallerybackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author liuh
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-04-29 23:25:04
 */
public interface UserService extends IService<User> {

    /**
     * 用户查询 -- 获取查询条件
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    Boolean userLogout(HttpServletRequest request);

    /**
     * @param userAccount  用户名
     * @param userPassword 密码
     * @param request      前端返回需要测试的用户名/密码
     * @return 脱敏后的用户信息 需要返回给前端
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户, 无须上传前端
     *
     * @param request 接收的信息
     * @return 数据
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注册
     *
     * @param userAccount   注册用户名
     * @param userPassword  注册密码
     * @param checkPassword 确认密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用于对用户密码的加密
     *
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);


    /**
     * 获得登录后的用户脱敏信息
     *
     * @param user 需要脱敏的信息
     * @return 脱敏成功的信息
     */
    LoginUserVO getLoginUserVO(User user);


    /**
     * 获得用户脱敏信息
     *
     * @param user 需要脱敏的信息
     * @return 脱敏成功的信息
     */
    UserVO getUserVO(User user);

    /**
     * 获得多名用户脱敏信息
     *
     * @param userList 需要脱敏的用户列表
     * @return 脱敏成功的信息
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 判断是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);
}
