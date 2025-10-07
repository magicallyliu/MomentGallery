package com.liuh.gallerybackend.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuh.gallerybackend.constant.UserConstant;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUtils;
import com.liuh.gallerybackend.mapper.UserMapper;
import com.liuh.gallerybackend.model.dto.user.UserQueryRequest;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.enums.UserRoleEnum;
import com.liuh.gallerybackend.model.vo.LoginUserVO;
import com.liuh.gallerybackend.model.vo.UserVO;
import com.liuh.gallerybackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 19627
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-04-29 23:25:04
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 用户注销
     *
     * @param request 前端返回数据
     * @return 返回true则表示注销成功
     */
    @Override
    public Boolean userLogout(HttpServletRequest request) {
        //获取域中的数据
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        //判断是否已经登录
        if (user == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }

        //移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户名
     * @param userPassword 密码
     * @param request      前端返回数据
     * @return 脱敏后的用户信息 需要返回给前端
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        /*
            1. 效验
            2. 对前端传递的密码进行加密
            3. 查询数据中的数据是否存在
            4. 记录用户的登录状态
        */
        //1. 效验
        // 参数不能为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        //账号长度不得低于4位
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "用户账户格式错误");
        //密码不得低于8位
        ThrowUtils.throwIf(userPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "用户密码格式错误");

        //2. 对前端传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);

        //3. 查询数据中的数据是否存在
        //查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);

        User user = this.baseMapper.selectOne(queryWrapper);

        //判断账户是否存在
        if (user == null || !encryptPassword.equals(user.getUserPassword())) {
            log.info("user login failed: The account does not exist or Wrong password");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不存在 或 密码错误");
        }

        //4. 记录用户的登录状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户, 无须上传前端
     *
     * @param request 接收的信息
     * @return 数完整据
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        //获取域中的数据
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        //判断是否已经登录
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        //检查用户id是否发生改变, 而不是一直使用缓存
        Long userId = user.getId();
        //和数据库的数据进行比对
        User byId = this.getById(userId);
        //为空表示数据库中不存在
        if (byId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    /**
     * 用户注册
     * <p>
     * 账号长度不得低于4位
     * 密码不得低于8位
     * 密码和确认密码需要一致
     *
     * @param userAccount   注册用户名
     * @param userPassword  注册密码
     * @param checkPassword 确认密码
     * @return 新用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        /*
        步骤
            1. 效验参数
            2. 检查用户账户是否和数据库中已有的重复
            3. 密码要加密
            4. 插入数据到数据库中
         */

        // 1. 效验参数,
        // 参数不能为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        //账号长度不得低于4位
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "用户账户过短");
        //密码不得低于8位
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "用户密码过短");
        //密码和确认密码需要一致
        ThrowUtils.throwIf(!checkPassword.equals(userPassword),
                ErrorCode.PARAMS_ERROR, "两次输入密码不一致");

        // 2. 检查用户账户是否和数据库中已有的重复
        //通过 baseMapper 调用Mapper
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);

        long count = this.baseMapper.selectCount(queryWrapper);
        //判断是否有的条件是查询出的条数
        ThrowUtils.throwIf(count >= 1,
                ErrorCode.PARAMS_ERROR, "用户已存在");

        // 3. 密码要加密
        String encryptPassword = getEncryptPassword(userPassword);

        //4. 插入数据到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        //设置用户的默认名称、用户角色
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());

        boolean saveResult = this.save(user);
        //为 true 代表插入数据库成功
        ThrowUtils.throwIf(!saveResult,
                ErrorCode.SYSTEM_ERROR, "插入数据库失败");


        return user.getId();
    }

    /**
     * 用于对用户密码的加密
     *
     * @param userPassword 需要加密的密码
     * @return 加密之后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        //加盐, 混淆密码
        final String SALT = "liuGallery";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 获得登录后的用户脱敏信息
     *
     * @param user 需要脱敏的信息
     * @return 脱敏成功的信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获得用户脱敏信息
     *
     * @param user 需要脱敏的信息
     * @return 脱敏成功的信息
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获得多名用户脱敏信息
     *
     * @param userList 需要脱敏的用户列表
     * @return 脱敏成功的信息
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        //如果列表为空, 则返回空列表
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList()); //转换为其他形式
    }


    /**
     * 用户查询 -- 获取查询条件
     *
     * @param userQueryRequest 查询所需要的表
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {

        //表示需要查询的为空
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询用户参数为空");
        }

        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        //定义查询条件
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        //当id不为空的时候, 进行查询
        userQueryWrapper.eq(ObjUtil.isNotNull(id), "id", id);

        userQueryWrapper.eq(StrUtil.isNotBlank(userRole),
                "userRole", userRole);
        userQueryWrapper.like(StrUtil.isNotBlank(userAccount),
                "userAccount", userAccount);
        userQueryWrapper.like(StrUtil.isNotBlank(userName),
                "userName", userName);
        userQueryWrapper.like(StrUtil.isNotBlank(userProfile),
                "userProfile", userProfile);
        //isAsc：一个布尔值，表示排序方向。true 表示升序（ASC），false 表示降序（DESC）
        userQueryWrapper.orderBy(StrUtil.isNotEmpty(sortField),
                sortOrder.equals("ascend"), sortField);
        return userQueryWrapper;
    }

    /**
     * 判断是否为管理员
     *
     * @param user 需要验证的用户名
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        //用户存在并且为管理员
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }


}




