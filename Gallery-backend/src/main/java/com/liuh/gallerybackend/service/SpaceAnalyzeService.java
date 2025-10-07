package com.liuh.gallerybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liuh.gallerybackend.model.dto.space.analyze.*;
import com.liuh.gallerybackend.model.entity.Space;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.vo.space.analyze.*;

import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/10/6 下午5:15
 * @PackageName com.liuh.gallerybackend.service
 * @ClassName SpaceAnalyService
 * @Version
 * @Description 针对表【picture(图片)】的数据库操作Service -- 用于空间分析
 */

@SuppressWarnings("all")

public interface SpaceAnalyzeService extends IService<Space> {


    /**
     * 获取空间使用情况分析
     *
     * @param spaceAnalyzeRequest
     * @param loginUser
     * @return
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片分类分析
     * 按照空间图片分类来进行分组
     * 统计每组的图片数量和大小
     *
     * @param spaceAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnzlyzeRequest spaceCategoryAnzlyzeRequest, User loginUser);

    /**
     * 获取空间图片标签分析
     * 返回每一个标签的使用次数
     *
     * @param spaceTagAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片大小分析
     * 更具需要查询图片大小范围 , 返回其图片的数量
     *
     * @param spaceAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 用户行为分析
     * 返回用户一段时间内的上传数量
     * 可针对一个用户返回
     *
     * @param spaceUserAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 获取空间使用排行(管理员)
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
