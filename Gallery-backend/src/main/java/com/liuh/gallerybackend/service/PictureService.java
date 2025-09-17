package com.liuh.gallerybackend.service;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.model.dto.picture.PictureQueryRequest;
import com.liuh.gallerybackend.model.dto.picture.PictureUploadRequest;
import com.liuh.gallerybackend.model.dto.user.UserQueryRequest;
import com.liuh.gallerybackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 19627
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-07-21 02:25:42
 */
public interface PictureService extends IService<Picture> {

    /**
     * 图片的上传
     *
     * @param multipartFile        前端上传的文件
     * @param pictureUploadRequest 图片的编码
     * @param user                 用户信息, 用于确定用户的 权限
     * @return 脱敏后的图片信息
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User user);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest 图片查询所需要的表
     * @return
     */
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 封装了获取图片的方法
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片包装类
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 数据效验
     * @param picture
     */
    void validPicture(Picture picture);
}
