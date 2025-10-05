package com.liuh.gallerybackend.service;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liuh.gallerybackend.common.BaseResponse;
import com.liuh.gallerybackend.common.DeleteRequest;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.model.dto.file.UploadPictureResult;
import com.liuh.gallerybackend.model.dto.picture.*;
import com.liuh.gallerybackend.model.dto.user.UserQueryRequest;
import com.liuh.gallerybackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.vo.PictureVO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 19627
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-07-21 02:25:42
 */
public interface PictureService extends IService<Picture> {

    /**
     * 图片的上传
     *
     * @param inputSource        输入源
     * @param pictureUploadRequest 图片的唯一标识
     * @param user                 用户信息, 用于确定用户的 权限
     * @return 脱敏后的图片信息
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User user);

    /**
     * 图片的删除
     * @param pictureId
     * @param loginUser
     * @return
     */
    void deletePicture( Long pictureId, User loginUser);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest 图片查询所需要的表
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

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

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核需要的信息
     * @param user                 审核人
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User user);

    /**
     * 填充图片审核参数
     *
     * @param picture
     * @param user
     */
    void fillReviewParams(Picture picture, User user);

    /**
     *   批量抓取创建的图片
     * @param pictureUploadByBatchRequest  批量抓取的图片
     * @param user                          用户信息
     * @return 返回成功的图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest  pictureUploadByBatchRequest, User user);

    /**
     * 清理存储桶中图片文件
     * 在删除图片, 更新图片时执行
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 效验空间图片权限 对于公共图库, 只有本人和管理员可以操作
     *  私有图库, 只有空间所有人可以操作
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     *  编辑图片信息
     * @param pictureEditRequest
     * @param loginUser
     * @return
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId   需要查询的空间
     * @param picColor  用户需要搜索的图片的颜色
     * @param loginUser 要求搜索的用户
     * @return
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量操作图片
     *
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);
//    /**
//     * 分页获取图片列表 -- 二级缓存版本
//     * @param pictureQueryRequest
//     * @return
//     */
//    Page<PictureVO> listPictureVOPageWithCache(PictureQueryRequest pictureQueryRequest);
}
