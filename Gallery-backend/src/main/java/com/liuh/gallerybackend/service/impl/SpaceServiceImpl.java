package com.liuh.gallerybackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUtils;
import com.liuh.gallerybackend.mananger.sharding.DynamicShardingManager;
import com.liuh.gallerybackend.model.dto.space.SpaceAddRequest;
import com.liuh.gallerybackend.model.dto.space.SpaceQueryRequest;
import com.liuh.gallerybackend.model.entity.Space;
import com.liuh.gallerybackend.model.entity.SpaceUser;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.enums.SpaceLevelEnum;
import com.liuh.gallerybackend.model.enums.SpaceRoleEnum;
import com.liuh.gallerybackend.model.enums.SpaceTypeEnum;
import com.liuh.gallerybackend.model.vo.SpaceVO;
import com.liuh.gallerybackend.model.vo.UserVO;
import com.liuh.gallerybackend.service.SpaceService;
import com.liuh.gallerybackend.mapper.SpaceMapper;
import com.liuh.gallerybackend.service.SpaceUserService;
import com.liuh.gallerybackend.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 19627
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-09-25 11:06:59
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService  spaceUserService;

    //暂时不使用分库分表
//    @Resource
//    @Lazy
//    private DynamicShardingManager dynamicShardingManager;
//    /**
//     * 编程事务管理器
//     */
//    @Resource
//    private TransactionTemplate transactionTemplate;
    /**
     * 空间锁, 用于控制同一个用户只能创建一个私有空间
     */
    Map<Long, Object> lockMap = new ConcurrentHashMap<>();

    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return 空间的id (如果失败返回 -1)
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // TODO 1. 填充参数默认值
        //转换参数为space
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        //空间名称的默认值
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        //空间级别的默认值
        if (ObjUtil.isNull(space.getSpaceLevel())) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        //空间类别的默认值
        if (ObjUtil.isNull(space.getSpaceType())) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        //填充空间的大小和容量
        this.fillSpaceBySpaceLevel(space);

        // TODO 2. 效验参数
        this.validSpace(space, true);

        //TODO 3. 效验权限, 非管理者只能调用普通基本的空间
        Long loginUserId = loginUser.getId();
        space.setUserId(loginUserId);
        //非管理员只能创建普通空间
        if (!space.getSpaceLevel().equals(SpaceLevelEnum.COMMON.getValue()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "非管理员不能创建高级空间");
        }


        //TODO 4. 控制同一个用户只能创建一个私有空间, 或者一个团队空间
        //为每一个用户创建不同的锁
        //多个用户可以同时执行创建私人空间
//        // intern()用于将字符串添加到字符串常量池（String Pool）中，并返回该字符串在常量池中的引用
//        //    用于保证每一个锁都相同
//        String lock = String.valueOf(loginUserId).intern();
//        synchronized (lock) {
//            //将数据库操作放在 编程事务管理器中, 防止锁释放了但是事务未提交
//            Long newSpaceId = transactionTemplate.execute(status -> {
//                boolean exists = this.lambdaQuery()
//                        .eq(Space::getUserId, loginUserId)
//                        .exists();
//                // 如果已有空间, 就不能再创建
//                ThrowUils.throwIf(exists, ErrorCode.PARAMS_ERROR, "用户只能创建一个私有空间");
//
//                //TODO 5. 插入到数据库
//                boolean result = this.save(space);
//                //TODO 6. 返回结果
//                ThrowUils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "创建空间失败");
//                //返回新写入的id
//                return space.getId();
//            });
//            //在创建失败的情况下, 返回-1
//            //如果创建成功, 返回新创建的id
//            return Optional.ofNullable(newSpaceId).orElse(-1L);
//        }
        //TODO 采用ConcurrentHashMap来存储对象
        //相比较前者,  后者可以防止内存泄漏
        Object lock = lockMap.computeIfAbsent(loginUserId, key -> new Object());
        synchronized (lock) {
            try {
                // 数据库操作
                //判断用户是否已经创建过空间
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, loginUserId)
                        .eq(Space::getSpaceType, space.getSpaceType())
                        .exists();
                // 如果已有空间, 就不能再创建
                ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "用户每类空间只能创建一个");

                //TODO 5. 插入到数据库
                boolean result = this.save(space);
                //TODO 6. 返回结果
                ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "创建空间失败");

                //创建成功后, 如果是团队空间, 关联新增团队成员记录
                if (space.getSpaceType().equals(SpaceTypeEnum.TEAM.getValue())) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(loginUserId);
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    result = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "创建团队成立失败");
                }
                //暂时不使用
//                //创建分表 -- 只针对团队旗舰版(方法内部判断)
//                dynamicShardingManager.createSpacePictureTable(space);
                //返回新写入的id
                return space.getId();
            } finally {
                // 防止内存泄漏
                lockMap.remove(loginUserId);
            }
        }
    }

    @Deprecated
    @Override
    public void deleteSpace(Long spaceId, User loginUser) {
    }

    @Override
    public void validSpace(Space space, boolean isAdd) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        //从 对象中取值
        //需要效验空间的空间名称, 空间级别
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        //判断级别是否符合
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);
        //判断空间类型是否为空
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum enumByType = SpaceTypeEnum.getEnumByValue(spaceType);

        //创建时效验
        if (isAdd) {
            //名称不能为空
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(ObjUtil.isNull(spaceLevel), ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            ThrowUtils.throwIf(ObjUtil.isNull(spaceType), ErrorCode.PARAMS_ERROR, "空间类型不能为空");
        }

        //空间名称存在, 并且不能大于30
        ThrowUtils.throwIf(StrUtil.isNotBlank(spaceName) && spaceName.length() > 30, ErrorCode.PARAMS_ERROR, "空间名称不能超过30个字符");
        //空间级别不能为空, 且级别存在
        ThrowUtils.throwIf(!ObjUtil.isNull(spaceLevel) && ObjUtil.isNull(enumByValue), ErrorCode.PARAMS_ERROR, "空间级别错误");
        ThrowUtils.throwIf(!ObjUtil.isNull(spaceType) && ObjUtil.isNull(enumByType), ErrorCode.PARAMS_ERROR, "空间类别不存在");
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;

    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        //获取查询数据列表
        List<Space> spaceList = spacePage.getRecords();
        //设置查询
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        //判断集合是否为空
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        //将空间集合中的对象装换为脱敏类
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        //1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        //根据用户id查询set表
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        //2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            //判断查询set表中是否存在key键符合id
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();


        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType),  "spaceType", spaceType);

        // 排序 默认升序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        //获取空间级别
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (!ObjUtil.isNull(spaceLevelEnum)) {
            //只有没有设置最大值时, 才会只有默认的基本最大值
            if (ObjUtil.isNull(space.getMaxCount())) {
                space.setMaxCount(spaceLevelEnum.getMaxCount());
            }
            if (ObjUtil.isNull(space.getMaxSize())) {
                space.setMaxSize(spaceLevelEnum.getMaxSize());
            }

        }
    }

    /**
     * 空间权限校验
     *
     * @param loginUser
     * @param space
     */
    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        // 仅本人或管理员可访问
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }


}




