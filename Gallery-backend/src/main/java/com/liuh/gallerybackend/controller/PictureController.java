package com.liuh.gallerybackend.controller;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.liuh.gallerybackend.annotation.AuthCheck;
import com.liuh.gallerybackend.api.imagesearch.ImageSearchApiFacade;
import com.liuh.gallerybackend.api.imagesearch.model.ImageSearchResult;
import com.liuh.gallerybackend.common.BaseResponse;
import com.liuh.gallerybackend.common.DeleteRequest;
import com.liuh.gallerybackend.common.ResultUtils;
import com.liuh.gallerybackend.constant.UserConstant;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUils;
import com.liuh.gallerybackend.model.dto.picture.*;
import com.liuh.gallerybackend.model.entity.Picture;
import com.liuh.gallerybackend.model.entity.Space;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.enums.PictureReviewStatusEnum;
import com.liuh.gallerybackend.model.vo.PictureTagCategory;
import com.liuh.gallerybackend.model.vo.PictureVO;
import com.liuh.gallerybackend.service.PictureService;
import com.liuh.gallerybackend.service.SpaceService;
import com.liuh.gallerybackend.service.UserService;
import com.qcloud.cos.model.ciModel.image.ImageSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author LiuH
 * @Date 2025/6/5 下午8:34
 * @PackageName com.liuh.gallerybackend.controller
 * @ClassName pictureController
 * @Version
 * @Description 文件上传和下载
 */

@SuppressWarnings("all")
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用于caffeine缓存
     * initialCapacity: 初始化的缓存大小
     * maximumSize: 缓存的最大数量
     * expireAfterWrite: 缓存的时间
     */
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();

    /**
     * 上传图片
     *
     * @param file                 上传的文件
     * @param pictureUploadRequest
     * @param response
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPictureByUrl
    (@RequestBody PictureUploadRequest pictureUploadRequest,
     HttpServletRequest request) {
        //获取当前用户登录的信息
        User loginUser = userService.getLoginUser(request);
        //得到文件的地址
        String fileUrl = pictureUploadRequest.getFileUrl();
        //上传图片
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过url上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/url")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture
    (@RequestParam("multipartFile") MultipartFile multipartFile,
     PictureUploadRequest pictureUploadRequest,
     HttpServletRequest request) {
        //获取当前用户登录的信息
        User loginUser = userService.getLoginUser(request);
        //上传图片
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        //请求删除的参数为空, 或者id不存在
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //判断权限, 本人或者管理员可以删除
        User loginUser = userService.getLoginUser(request);
        pictureService.deletePicture(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 修改数据
     *
     * @param pictureUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
                                               HttpServletRequest request) {
        //判断需要修改的数据是否为空, 以及修改对象不能低于0
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //将图片信息提取出来
        Picture picture = new Picture();
        //将pictureUpdateRequest 中的数据 复制到 picture 中去 (仅仅复制两者相同的字符)
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        //JSONUtil.toJsonStr可以将任意对象（Bean、Map、集合等）直接转换为JSON字符串。
        // 如果对象是有序的Map等对象，则转换后的JSON字符串也是有序的
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));

        //数据效验
        pictureService.validPicture(picture);

        //判断数据
        Long id = pictureUpdateRequest.getId();
        Picture byId = pictureService.getById(id);
        ThrowUils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR);
        //获取用户信息
        User loginUser = userService.getLoginUser(request);
        //添加审核参数
        pictureService.fillReviewParams(picture, loginUser);

        //操作数据库
        boolean b = pictureService.updateById(picture);
        ThrowUils.throwIf(b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询图片, 面向管理员, 无须脱敏
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(Long id, HttpServletRequest request) {
        ThrowUils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Picture byId = pictureService.getById(id);
        ThrowUils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(byId);

    }

    /**
     * 获取图片, 面向用户
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/VO")
    public BaseResponse<PictureVO> getPictureVOById(Long id, HttpServletRequest request) {
        ThrowUils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库
        Picture byId = pictureService.getById(id);
        ThrowUils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR);
        //效验权限
        //公共图片都可以访问, 私有图片只有本人可以访问
        if (!ObjUtil.isNull(byId.getSpaceId())) {
            pictureService.checkPictureAuth(userService.getLoginUser(request), byId);
        }

        return ResultUtils.success(pictureService.getPictureVO(byId, request));
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     *
     * @param pictureQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        //参数不存在则不执行查询
        ThrowUils.throwIf(pictureQueryRequest == null,
                ErrorCode.PARAMS_ERROR, "分页查询参数错误");

        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVoByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUils.throwIf(size > 100, ErrorCode.PARAMS_ERROR);

        //空间权限效验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (ObjUtil.isNull(spaceId)) {
            //公共图库
            //普通用户默认只能看到审核通过的数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            //只能查看公共图片
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            //私有空间
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            //判断空间是否存在
            ThrowUils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR);
            //判断权限
            //只有空间所有人可以访问
            if (!space.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 分页获取图片列表 -- 二级缓存版本
     * 暂时不使用
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @Deprecated
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                    HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUils.throwIf(size > 100, ErrorCode.PARAMS_ERROR);
        //普通用户默认只能看到审核通过的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        //查询缓存, 缓存中不存在, 再查询数据库
        //将查询对象装换为json字符串
        String jsonStr = JSONUtil.toJsonStr(pictureQueryRequest);
        //        //将其转换为哈希值, 并以16位16进制字符串的形式返回
        String hashKey = DigestUtils.md5DigestAsHex(jsonStr.getBytes());
        //每次查询的键值
        String cachedKey = String.format("listPictureVObyPage:%s", hashKey);

        //先查询本地缓存
        String cachedValue = LOCAL_CACHE.getIfPresent(cachedKey);
        if (StrUtil.isNotBlank(cachedValue)) {
            //如果存在, 则直接返回缓存中的数据
            return ResultUtils.success(JSONUtil.toBean(cachedValue, Page.class));
        }
        //从分布式缓存中查询
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        //根据key值查询数据
        cachedValue = opsForValue.get(cachedKey);
        //判断是否存在缓存中
        if (StrUtil.isNotBlank(cachedValue)) {
            //如果存在, 则更新本地缓存, 并且返回结果
            LOCAL_CACHE.put(cachedKey, cachedValue);
            return ResultUtils.success(JSONUtil.toBean(cachedValue, Page.class));
        }


        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        //获取封装类
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);

        //将查询到的数据转换为JSON字符串
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        //设置缓存的过期时间 (5 ~ 10分钟) --防止缓存雪崩
        int timeout = 300 + RandomUtil.randomInt(0, 300);
        //将查询到的数据写入分布式缓存中, 避免下次查询再次查询数据库
        //其中, timeout 为过期时间, TimeUnit.SECONDS 为时间单位秒
        opsForValue.set(hashKey, cacheValue, timeout, TimeUnit.SECONDS);
        //写入本地缓存
        LOCAL_CACHE.put(cachedKey, cacheValue);
        // 获取封装类
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 分页获取图片列表 -- 有缓存版本(分布式缓存)
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @Deprecated
    @PostMapping("/list/page/vo/redis")
    public BaseResponse<Page<PictureVO>> listPictureVOpageWithRedis(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                    HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUils.throwIf(size > 100, ErrorCode.PARAMS_ERROR);
        //普通用户默认只能看到审核通过的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        //查询缓存, 缓存中不存在, 再查询数据库
        //将查询对象装换为json字符串
        String jsonStr = JSONUtil.toJsonStr(pictureQueryRequest);
        //将其转换为哈希值, 并以16位16进制字符串的形式返回
        String hashKey = DigestUtils.md5DigestAsHex(jsonStr.getBytes());
        //每次查询的键值
        String redisKey = String.format("momentGallery:listPictureVObyPage:%s", hashKey);
        //从缓存中查询
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        //根据key值查询数据
        String cachedValue = opsForValue.get(redisKey);
        //判断是否存在缓存中
        if (StrUtil.isNotBlank(cachedValue)) {
            //如果存在, 则直接返回缓存中的数据
            return ResultUtils.success(JSONUtil.toBean(cachedValue, Page.class));
        }


        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        //获取封装类
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);

        //将查询到的数据转换为JSON字符串
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        //设置缓存的过期时间 (5 ~ 10分钟) --防止缓存雪崩
        int timeout = 300 + RandomUtil.randomInt(0, 300);
        //将查询到的数据写入缓存中, 避免下次查询再次查询数据库
        //其中, timeout 为过期时间, TimeUnit.SECONDS 为时间单位秒
        opsForValue.set(hashKey, cacheValue, timeout, TimeUnit.SECONDS);

        // 获取封装类
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 分页获取图片列表 -- 有缓存版本(本地缓存)
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @Deprecated
    @PostMapping("/list/page/vo/caffeine")
    public BaseResponse<Page<PictureVO>> listPictureVOpageWithCaffeine(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                       HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUils.throwIf(size > 100, ErrorCode.PARAMS_ERROR);
        //普通用户默认只能看到审核通过的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        //查询缓存, 缓存中不存在, 再查询数据库
        //将查询对象装换为json字符串
        String jsonStr = JSONUtil.toJsonStr(pictureQueryRequest);
        //        //将其转换为哈希值, 并以16位16进制字符串的形式返回
        String hashKey = DigestUtils.md5DigestAsHex(jsonStr.getBytes());
        //每次查询的键值
        String cachedKey = String.format("listPictureVObyPage:%s", hashKey);
        //从缓存中查询
//        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        //根据key值查询数据
//        String cachedValue = opsForValue.get(cachedKey);
        //本地缓存查询
        String cachedValue = LOCAL_CACHE.getIfPresent(cachedKey);
        //判断是否存在缓存中
        if (StrUtil.isNotBlank(cachedValue)) {
            //如果存在, 则直接返回缓存中的数据
            return ResultUtils.success(JSONUtil.toBean(cachedValue, Page.class));
        }


        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        //获取封装类
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);

        //将查询到的数据转换为JSON字符串
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        //设置缓存的过期时间 (5 ~ 10分钟) --防止缓存雪崩
        int timeout = 300 + RandomUtil.randomInt(0, 300);
        //将查询到的数据写入缓存中, 避免下次查询再次查询数据库
        //其中, timeout 为过期时间, TimeUnit.SECONDS 为时间单位秒
//        opsForValue.set(hashKey,cacheValue, timeout, TimeUnit.SECONDS);
        //写入本地缓存
        LOCAL_CACHE.put(cachedKey, cacheValue);
        // 获取封装类
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 编辑图片（给用户使用）
     *
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 展示展示的热门标签
     *
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("测试", "热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("测试", "模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


    /**
     * 审核图片
     *
     * @param pictureReviewRequest 审核图片的数据
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        //参数不存在则不执行查询
        ThrowUils.throwIf(pictureReviewRequest == null,
                ErrorCode.PARAMS_ERROR, "审核图片参数错误");

        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量抓取并获取图片
     *
     * @param pictureUploadByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                      HttpServletRequest request) {
        //参数不存在则不执行查询
        ThrowUils.throwIf(pictureUploadByBatchRequest == null,
                ErrorCode.PARAMS_ERROR, "审核图片参数错误");

        User loginUser = userService.getLoginUser(request);
        Integer integer = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(integer);
    }

    /**
     * 根据图片识别图片
     * 上传图库中存在的图片去查询相似的图片
     *
     * @param searchPictureByPictureRequest
     * @param request
     * @return
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(
            @RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest
            , HttpServletRequest request) {

        //参数不存在则不执行查询
        ThrowUils.throwIf(searchPictureByPictureRequest == null,
                ErrorCode.PARAMS_ERROR);

        //实现以图搜图
        Long searchPictureId = searchPictureByPictureRequest.getPictureId();
        //图片id存在在图库中
        ThrowUils.throwIf(searchPictureId == null
                || searchPictureId <= 0, ErrorCode.PARAMS_ERROR);
        //查询图片
        Picture picture = pictureService.getById(searchPictureId);
        //图片不存在
        ThrowUils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        //获取相似图片
        List<ImageSearchResult> imageSearchResults = ImageSearchApiFacade.searchImage(picture.getUrl());
        //返回相似图片
        return ResultUtils.success(imageSearchResults);
    }

    /**
     * 根据图片颜色搜索图片
     * 用户选择一种颜色, 根据选择的颜色返回一定数量的图片
     *
     * @param searchPictureByColorRequest
     * @param request
     * @return
     */
    @PostMapping("/search/color")
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest,
                                                              HttpServletRequest request) {
        //参数不存在则不执行查询
        ThrowUils.throwIf(ObjUtil.isEmpty(searchPictureByColorRequest),
                ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        //获取需要的参数并执行
        List<PictureVO> searchPictureVOByColor = pictureService.searchPictureByColor(
                searchPictureByColorRequest.getSpaceId(), searchPictureByColorRequest.getPicColor(), loginUser);
        //返回查询结果
        return ResultUtils.success(searchPictureVOByColor);
    }


    @PostMapping("/edit/batch")
    public BaseResponse<Boolean> editPictureByBatch(
            @RequestBody PictureEditByBatchRequest pictureEditByBatchRequest,
            HttpServletRequest request) {
        //判断是否为空
        ThrowUils.throwIf(ObjUtil.isEmpty(pictureEditByBatchRequest),
                ErrorCode.PARAMS_ERROR);
        //获取用户信息
        User loginUser = userService.getLoginUser(request);
        //执行修改
        pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

}














