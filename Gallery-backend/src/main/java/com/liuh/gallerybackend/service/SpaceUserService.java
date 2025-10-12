package com.liuh.gallerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liuh.gallerybackend.model.dto.space.SpaceAddRequest;
import com.liuh.gallerybackend.model.dto.space.SpaceQueryRequest;
import com.liuh.gallerybackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.liuh.gallerybackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.liuh.gallerybackend.model.entity.Space;
import com.liuh.gallerybackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.vo.SpaceUserVO;
import com.liuh.gallerybackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 19627
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-10-07 19:50:31
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 创建空间成员
     *
     * @param spaceUserAddRequest
     * @return 空间的id
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);


    /**
     * 效验空间成员
     *
     * @param spaceUser
     * @param isAdd     是否为创建时效验，true为创建，false为修改
     */
    void validSpaceUser(SpaceUser spaceUser, boolean isAdd);

    /**
     * 封装了获取空间成员信息的方法
     *
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间成员包装类
     *
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    /**
     * 获取查询对象
     *
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);


}
