package com.liuh.gallerybackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUtils;
import com.liuh.gallerybackend.mapper.SpaceMapper;
import com.liuh.gallerybackend.model.dto.space.analyze.*;
import com.liuh.gallerybackend.model.entity.Picture;
import com.liuh.gallerybackend.model.entity.Space;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.vo.space.analyze.*;
import com.liuh.gallerybackend.service.PictureService;
import com.liuh.gallerybackend.service.SpaceAnalyzeService;
import com.liuh.gallerybackend.service.SpaceService;
import com.liuh.gallerybackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author LiuH
 * @Date 2025/10/6 下午5:17
 * @PackageName com.liuh.gallerybackend.service.impl
 * @ClassName SpaceAnalyServiceImpl
 * @Version
 * @Description
 */

@SuppressWarnings("all")
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private PictureService pictureService;
    @Autowired
    private SpaceMapper spaceMapper;


    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
        //效验参数

        //判断查询范围
        //全空间或者公共需要从picuture表查询
        //私人空间直接从space表查询
        if (spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()) {
            //权限效验
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
            //封装查询条件 - -统计公共图库的使用空间
            QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
            pictureQueryWrapper.select("picSize");

            //封装查询条件
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, pictureQueryWrapper);

            List<Object> objects = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper);
            long usedSize = objects.stream().mapToLong(Obj -> (Long) Obj).sum();
            long usedCount = objects.size();
            //封装返回结果
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            // 公共图库无上限、无比例
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);
            return spaceUsageAnalyzeResponse;
        } else {
            // 查询指定空间
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(ObjUtil.isNull(spaceId) || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            // 获取空间信息
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR, "空间不存在");

            // 权限校验：仅空间所有者或管理员可访问
            spaceService.checkSpaceAuth(loginUser, space);

            // 构造返回结果
            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            response.setUsedSize(space.getTotalSize());
            response.setMaxSize(space.getMaxSize());
            // 后端直接算好百分比，这样前端可以直接展示
            double sizeUsageRatio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            response.setSizeUsageRatio(sizeUsageRatio);
            response.setUsedCount(space.getTotalCount());
            response.setMaxCount(space.getMaxCount());
            double countUsageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            response.setCountUsageRatio(countUsageRatio);
            return response;
        }
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnzlyzeRequest spaceCategoryAnzlyzeRequest, User loginUser) {
        //参数不能为空
        ThrowUtils.throwIf(ObjUtil.isNull(spaceCategoryAnzlyzeRequest), ErrorCode.PARAMS_ERROR);
        //检查权限
        checkSpaceAnalyzeAuth(spaceCategoryAnzlyzeRequest, loginUser);
        //构造查询条件
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnzlyzeRequest, pictureQueryWrapper);

        //分组查询
        pictureQueryWrapper.select("category", "count(*) as count", "sum(picSize) as size")
                .groupBy("category");

        return pictureService.getBaseMapper().selectMaps(pictureQueryWrapper)
                .stream()
                .map(result -> {
                    String category = (String) result.get("category");
                    Long count = ((Number) result.get("count")).longValue();
                    Long size = ((Number) result.get("size")).longValue();
                    return new SpaceCategoryAnalyzeResponse(category, count, size);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 检查权限
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);

        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);

        // 查询所有符合条件的标签
        queryWrapper.select("tags");
        List<String> tagsJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());

        // 合并所有标签并统计使用次数
        Map<String, Long> tagCountMap = tagsJsonList.stream()
                .flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        // 转换为响应对象，按使用次数降序排序
        return tagCountMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue())) // 降序排列
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }


    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceSizeAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        //检查权限
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        //构造查询条件
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, pictureQueryWrapper);

        //查询符合条件的图片大小
        pictureQueryWrapper.select("picSize");
        List<Long> pictureSizeList = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper)
                .stream()
                .filter(ObjUtil::isNotEmpty)
                .map(size -> (Long) size)
                .collect(Collectors.toList());

        //定义分段范围, 使用有序的map
        LinkedHashMap<String, Long> sizePicture = new LinkedHashMap<>();
        sizePicture.put("<100KB", pictureSizeList.stream().filter(size -> size < 100 * 1024).count());
        sizePicture.put("100KB-500KB", pictureSizeList.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizePicture.put("500KB-1MB", pictureSizeList.stream().filter(size -> size >= 500 * 1024 && size < 1 * 1024 * 1024).count());
        sizePicture.put(">1MB", pictureSizeList.stream().filter(size -> size >= 1 * 1024 * 1024).count());

        //转换响应对象
        return sizePicture.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceUserAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        //检查权限
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        //构造查询条件
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, pictureQueryWrapper);

        //用户id查询 -- 如果存在id
        Long userId = spaceUserAnalyzeRequest.getUserId();
        pictureQueryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);

        //补充分析维度 ;日, 周 月
        String dimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (dimension) {
            case "day":
                pictureQueryWrapper.select("date_format(createTime, '%Y-%m-%d') as date", "count(*) as count");
                break;
            case "week":
                pictureQueryWrapper.select("date_format(createTime, '%Y-%u') as date", "count(*) as count");
                break;
            case "month":
                pictureQueryWrapper.select("date_format(createTime, '%Y-%m') as date", "count(*) as count");
                break;
            default:
               throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }

        //分组排序
        pictureQueryWrapper.groupBy("date").orderByAsc("date");
        //查询
        return pictureService.getBaseMapper().selectMaps(pictureQueryWrapper)
                .stream()
                .map(result -> {
                    String date = (String) result.get("date");
                    Long count = ((Number) result.get("count")).longValue();
                    return new SpaceUserAnalyzeResponse(date, count);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceRankAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        //检查权限, 仅管理员可以查看
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);

        //构造查询条件
        QueryWrapper<Space> spaceQueryWrapper = new QueryWrapper<>();
        spaceQueryWrapper.select("id", "spaceName","userId","totalSize")
                .orderByDesc("totalSize")
                .last("limit " + spaceRankAnalyzeRequest.getTopN());

        return spaceService.list(spaceQueryWrapper);
    }

    /**
     * 效验空间分析权限
     *
     * @param spaceAnalyzeRequest
     * @param loginUser
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        //获取数据
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        //如果需要效验公共图库, 和所有空间, 只有管理员可以操作
        if (queryAll || queryPublic) {
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        }
        //如果需要私人效验空间, 则需要判断空间是否是自己的或者管理员访问
        else {
            //判断空间是否存在
            ThrowUtils.throwIf(ObjUtil.isNull(spaceId), ErrorCode.PARAMS_ERROR);
            Space space = this.getById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR);
            //效验
            spaceService.checkSpaceAuth(loginUser, space);
        }
    }

    /**
     * 根据请求条件封装查询条件
     * 三种请求方式:
     * 1. 管理员全空间分析
     * 2. 管理员公共图库分析
     * 3. 用户私人空间分析
     *
     * @param spaceAnalyzeRequest
     * @param queryWrapper
     */
    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        if (spaceAnalyzeRequest.isQueryAll()) {
            return;
        }
        if (spaceAnalyzeRequest.isQueryPublic()) {
            queryWrapper.isNull("spaceId");
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }



}
