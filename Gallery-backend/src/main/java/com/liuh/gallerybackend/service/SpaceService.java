package com.liuh.gallerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liuh.gallerybackend.common.BaseResponse;
import com.liuh.gallerybackend.common.DeleteRequest;
import com.liuh.gallerybackend.model.dto.space.SpaceAddRequest;
import com.liuh.gallerybackend.model.dto.space.SpaceQueryRequest;
import com.liuh.gallerybackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.vo.SpaceVO;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 19627
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-09-25 11:06:59
 */
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     * @param spaceAddRequest
     * @param loginUser
     * @return  空间的id
     */
    long addSpace(SpaceAddRequest  spaceAddRequest, User  loginUser);

    @Deprecated
    void deleteSpace(Long spaceId, User loginUser);
    /**
     * space数据效验
     *
     * @param space
     * @param isAdd 是否为创建时效验，true为创建，false为修改
     */
    void validSpace(Space space, boolean isAdd);

    /**
     * 封装了获取空间信息的方法
     *
     * @param space
     * @param request
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 分页获取空间包装类
     *
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取查询对象
     *
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     *  根据空间级别填充空间信息
     *  最大图片数量, 最大存储空间
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);
}
