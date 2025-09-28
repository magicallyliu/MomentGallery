package com.liuh.gallerybackend.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.liuh.gallerybackend.common.BaseResponse;
import com.liuh.gallerybackend.common.DeleteRequest;
import com.liuh.gallerybackend.common.ResultUtils;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUils;
import com.liuh.gallerybackend.mananger.CosManager;
import com.liuh.gallerybackend.mananger.FileManager;
import com.liuh.gallerybackend.mananger.upload.FilePictureUpload;
import com.liuh.gallerybackend.mananger.upload.PictureUploadTemplate;
import com.liuh.gallerybackend.mananger.upload.UrlPictureUpload;
import com.liuh.gallerybackend.mapper.PictureMapper;
import com.liuh.gallerybackend.model.dto.file.UploadPictureResult;
import com.liuh.gallerybackend.model.dto.picture.*;
import com.liuh.gallerybackend.model.entity.Picture;
import com.liuh.gallerybackend.model.entity.Space;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.enums.PictureReviewStatusEnum;
import com.liuh.gallerybackend.model.vo.PictureVO;
import com.liuh.gallerybackend.model.vo.UserVO;
import com.liuh.gallerybackend.service.PictureService;
import com.liuh.gallerybackend.service.SpaceService;
import com.liuh.gallerybackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 19627
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-07-21 02:25:42
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

//    //用于caffeine缓存
//    //initialCapacity: 初始化的缓存大小
//    //maximumSize: 缓存的最大数量
//    //expireAfterWrite: 缓存的时间
//    private final Cache<String, String> LOCAL_CACHE =
//            Caffeine.newBuilder().initialCapacity(1024)
//                    .maximumSize(10000L)
//                    // 缓存 5 分钟移除
//                    .expireAfterWrite(5L, TimeUnit.MINUTES)
//                    .build();
    /**
     * 已经弃用
     *
     * @deprecated
     */
    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    /**
     * 编程式事务
     */
    @Resource
    private TransactionTemplate transactionTemplate;

    @Autowired
    private CosManager cosManager;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //1. 效验参数
        ThrowUils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        //效验空间是否存在
        //如果是上传图片到私有图库, 则需要判断空间是否存在
        //同时, 必须是空间所有人才可以创建
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            //判断是否存在
            Space space = spaceService.getById(spaceId);
            ThrowUils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            ;
            //判断空间所有人
            ThrowUils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "没有权限");

            //判断空间是否有额度
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数已满");
            }

            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小已满");
            }
        }

        //2. 判断是新增还是更新
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        //如果是更新,判断图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

            //只有管理员和本人可以编辑图片
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }

            //效验上传空间是否一致
            //如果上传到公共图库, 则不需要验证
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                //如果上传到私有图库, 则需要验证
                ThrowUils.throwIf(!spaceId.equals(oldPicture.getSpaceId()), ErrorCode.NO_AUTH_ERROR, "空间不一致");
            }
        }

        //3. 上传图片, 得到图片信息
        //根据用户id划分目录 --> 按照路径划分
        String uploadPathPrefix;
        //如果上传到公共图库,使用public目录
        //上传到私有空间, 则使用空间id
        if (spaceId == null) {
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            uploadPathPrefix = String.format("space/%s", spaceId);
        }

        //根据不同的输入源, 调用不同的上传方法
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        //如果为url, 调用url上传方法
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        //构造入库的图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        String picName = uploadPictureResult.getPicName();
        //如果外层传来图片名称,  优先使用
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setSpaceId(spaceId);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        //添加审核参数
        this.fillReviewParams(picture, loginUser);


        //4. 操作数据库
        //如果 pictureId 不为空, 表示更新, 否则是新增加
        if (pictureId != null) {
            // 如果是更新, 需要补充 id和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
            //清理图片资源
            this.clearPictureFile(picture);
        }
        //开启事务
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            //插入数据
            boolean b = this.saveOrUpdate(picture);
            //如果数据未插入, 则不更新用户空间额度
            ThrowUils.throwIf(!b, ErrorCode.OPERATION_ERROR, "图片上传失败");
            //更新额度 --私有空间
            if (ObjUtil.isNotNull(finalSpaceId)) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize +  " + picture.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新额度失败");
            }
            //返回结果
            return b;
        });


        //注解属性值存在则更新记录，否插入一条记录
        //结果为是否插入成功

        return PictureVO.objToVo(picture);
    }

    @Override
    public void deletePicture(Long pictureId, User loginUser) {

        //已经存在的图片
        Picture oldPicture = this.getById(pictureId);
        //判断图片是否存在
        ThrowUils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        //判断权限
        //采用方法二
        //仅本人和管理员可以删除
//        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
        //方法二
        this.checkPictureAuth(loginUser, oldPicture);


        //在数据库中删除该图片
        //在删除图片中, 还需要释放额度
        transactionTemplate.execute(status -> {
            boolean removeById = this.removeById(pictureId);
            ThrowUils.throwIf(!removeById, ErrorCode.OPERATION_ERROR, "删除失败");

            //私有空间, 需要释放额度
            //更新额度
            if (ObjUtil.isNotNull(oldPicture.getSpaceId())) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, oldPicture.getSpaceId())
                        .setSql("totalSize = totalSize -  " + oldPicture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新额度失败");

            }
            //返回结果
            return removeById;
        });


        //删除存储桶的资源
        this.clearPictureFile(oldPicture);
    }

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest 图片查询所需要的表(参数)
     * @return
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值

        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Date reviewTime = pictureQueryRequest.getReviewTime();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            //name 和 introduction 的模糊查询
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotEmpty(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 封装了获取图片的方法
     *
     * @param picture
     * @param request
     * @return
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片包装类
     *
     * @param picturePage 分页参数
     * @param request
     * @return
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        //获取查询数据列表
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        //根据用户id查询
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * 数据效验
     *
     * @param picture
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        //判断上传数据超过范围
        if (StrUtil.isNotBlank(url)) {
            ThrowUils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User user) {
        //1. 效验参数
        ThrowUils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        // 用于确定传入的参数为 0 1 2 中的, 以用于判断是否为空
        PictureReviewStatusEnum anEnum = PictureReviewStatusEnum.getEnum(reviewStatus);
        //判断请求基本参数是否存在, 同时不能将审核状态改为待审核
        if (id == null || anEnum == null || PictureReviewStatusEnum.REVIEWING.equals(anEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //2. 判断图片是否存在
        Picture byId = this.getById(id);
        ThrowUils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR);
        //3. 效验审核状态是否重复
        if (byId.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }
        //4. 数据库操作
        //将需要修改的数据填充到新的表中, 以方便修改
        Picture updatePicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(user.getId());
        updatePicture.setReviewTime(new Date());
        boolean b = this.updateById(updatePicture);
        ThrowUils.throwIf(!b, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void fillReviewParams(Picture picture, User user) {
        //如果是管理员, 就自动过审
        //如果不是, 无论是编辑还是创建都是未审核
        if (userService.isAdmin(user)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(user.getId());
            picture.setReviewTime(new Date());
            picture.setReviewMessage("管理员自动过审");
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        //1.  效验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        // 格式化数量
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "单次上传数量不能超过30");
        //名称前缀默认等于搜索关键词
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }


        //2. 抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        //获取并解析一个html文件
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }

        //3. 解析内容
        Element div = document.getElementsByClass("dgControl").first();
        //如果最外层不存在, 则都不存在
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        //获取图片  取img图片标签同时要具有mimg 这个类名才会被选择到
        Elements imgElementList = div.select("img.mimg");
        //遍历元素中的图片, 依次上传图片
        //上传成功的数量
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            //获取图片元素的的地址
            String fileUrl = imgElement.attr("src");
            //如果地址不存在, 则跳过
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            //处理图片的地址, 防止转义和对象存储冲突的问题
            //如 code.cn?lih=1.  应该只保留code.cn
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }

            //4. 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            //填充图片的地址
            pictureUploadRequest.setFileUrl(fileUrl);
            //构造图片名称
            pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            //5. 如果上传的数量超过限制, 则停止上传
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    //@async 异步操作 需要 @EnableAsync
    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断改图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery()
                .eq(Picture::getUrl, pictureUrl)
                .count();
        // 有不止一条记录用到了该图片，不清理
        if (count > 1) {
            return;
        }
        // 删除图片
        cosManager.deleteObject(pictureUrl);
        // 删除缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }


    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        Long loginUserId = loginUser.getId();
        //如果是公共图库, 则本人和管理员可以上传
        if (ObjUtil.isNull(spaceId)) {
            if (!userService.isAdmin(loginUser) && !loginUserId.equals(picture.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            //私有空间仅所有人可以访问
            ThrowUils.throwIf(!loginUserId.equals(picture.getUserId()), ErrorCode.NO_AUTH_ERROR);
        }
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        //1. 效验参数
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);
        //添加审核参数
        this.fillReviewParams(picture, loginUser);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
//        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
        //使用新效验方法
        this.checkPictureAuth(loginUser, oldPicture);
        // 操作数据库
        boolean result = this.updateById(picture);
        ThrowUils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }
//
//    @Override
//    public Page<PictureVO> listPictureVOPageWithCache(PictureQueryRequest pictureQueryRequest) {
//        //查询缓存, 缓存中不存在, 再查询数据库
//        //将查询对象装换为json字符串
//        String jsonStr = JSONUtil.toJsonStr(pictureQueryRequest);
//        //        //将其转换为哈希值, 并以16位16进制字符串的形式返回
//        String hashKey = DigestUtils.md5DigestAsHex(jsonStr.getBytes());
//        //每次查询的键值
//        String cachedKey = String.format("listPictureVObyPage:%s", hashKey);
//
//        //先查询本地缓存
//        String cachedValue = LOCAL_CACHE.getIfPresent(cachedKey);
//        if (StrUtil.isNotBlank(cachedValue)){
//            //如果存在, 则直接返回缓存中的数据
//            return JSONUtil.toBean(cachedValue,Page.class);
//        }
//        //从分布式缓存中查询
//        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
//        //根据key值查询数据
//        cachedValue = opsForValue.get(cachedKey);
//        //判断是否存在缓存中
//        if (StrUtil.isNotBlank(cachedValue)){
//            //如果存在, 则更新本地缓存, 并且返回结果
//            LOCAL_CACHE.put(cachedKey, cachedValue);
//            return ResultUtils.success(JSONUtil.toBean(cachedValue,Page.class));
//        }
//
//
//        // 查询数据库
//        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
//                pictureService.getQueryWrapper(pictureQueryRequest));
//        //获取封装类
//        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
//
//        //将查询到的数据转换为JSON字符串
//        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
//        //设置缓存的过期时间 (5 ~ 10分钟) --防止缓存雪崩
//        int timeout = 300 + RandomUtil.randomInt(0, 300);
//        //将查询到的数据写入分布式缓存中, 避免下次查询再次查询数据库
//        //其中, timeout 为过期时间, TimeUnit.SECONDS 为时间单位秒
//        opsForValue.set(hashKey,cacheValue, timeout, TimeUnit.SECONDS);
//        //写入本地缓存
//        LOCAL_CACHE.put(cachedKey, cacheValue);
//        return null;
//    }
}




