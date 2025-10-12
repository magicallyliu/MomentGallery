package com.liuh.gallerybackend.mananger.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.liuh.gallerybackend.constant.UserConstant;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.mananger.auth.model.SpaceUserPermission;
import com.liuh.gallerybackend.mananger.auth.model.SpaceUserPermissionConstant;
import com.liuh.gallerybackend.model.entity.Picture;
import com.liuh.gallerybackend.model.entity.Space;
import com.liuh.gallerybackend.model.entity.SpaceUser;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.enums.SpaceRoleEnum;
import com.liuh.gallerybackend.model.enums.SpaceTypeEnum;
import com.liuh.gallerybackend.service.PictureService;
import com.liuh.gallerybackend.service.SpaceService;
import com.liuh.gallerybackend.service.SpaceUserService;
import com.liuh.gallerybackend.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    /**
     * 当前请求的上下文路径 获取Controller的初始路径
     * context-path: /api  #指定上下文路径
     */
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private SpaceUserAuthManger spaceUserAuthManger;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        //仅对空间用户进行权限验证
        //如果不为空间的, 直接返回空
        if (!loginType.equals(StpKit.SPACE_TYPE)) {
            return new ArrayList<>();
        }

        //获取管理员权限
        List<String> ADMIN_PERMISSIONS = spaceUserAuthManger.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        //获取上下文
        SpaceUserAuthContext authContext = getAuthContextByRequest();
        //  如果所有字段为空, 表示此时查询公共图库, 直接返回管理员权限
        if (this.isAllFieldNull(authContext)) {
            return ADMIN_PERMISSIONS;
        }

        //获取userid
        //获取登录是加载的User信息
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(UserConstant.USER_LOGIN_STATE);
        if (ObjUtil.isNull(loginUser)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        Long loginUserId = loginUser.getId();

        // 优先获取SpaceUser对象, 当其存在时, 直接通过其获取用户角色
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (ObjUtil.isNotNull(spaceUser)) {
            //根据用户角色返回对应权限
            return spaceUserAuthManger.getPermissionsByRole(spaceUser.getSpaceRole());
        }

        // 在未直接获取到SpaceUser对象时
        // 如果存在SpaceUserid -- 存在空间用户列表, 则为团队空间, 获取该用户对应的SpaceUser列表
        Long spaceUserId = authContext.getSpaceUserId();
        if (ObjUtil.isNotNull(spaceUserId)) {
            //根据传入的上下文参数, 获取对应的spaceUser对象
            spaceUser = spaceUserService.getById(spaceUserId);


            if (ObjUtil.isNull(spaceUser)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到空间用户信息");
            }
            //返回当前登录用户对应的spaceUser对象
            SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                    .eq(SpaceUser::getUserId, loginUserId)
                    .one();//返回查询到等等单个结果, 无结果/多个结果不返回

            //当未找到当前登录用户对应的spaceUser, 则返回空权限
            if (ObjUtil.isNull(loginSpaceUser)) {
                return new ArrayList<>();
            }
            //根据用户角色返回对应权限
            return spaceUserAuthManger.getPermissionsByRole(loginSpaceUser.getSpaceRole());
        }

        // 当spaceUserId不存在时, 通过spaceId和pictureId获取图片或图片信息
        // 根据图片判断
        Long spaceId = authContext.getSpaceId();
        //当spaceId为空时, 通过picture查询图片信息
        if (ObjUtil.isNull(spaceId)) {

            Long pictureId = authContext.getPictureId();
            //当pictureId和spaceId都为空时, 默认为管理员权限
            //此时仅在操作公共图库, 未查询图片信息等
            if (ObjUtil.isNull(pictureId)) {
                return ADMIN_PERMISSIONS;
            }
            //根据图片信息获取空间信息
            Picture loginPicture = pictureService.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .select(Picture::getId, Picture::getSpaceId, Picture::getUserId)
                    .one();

            //当未找到图片信息时, 返回空权限
            if (ObjUtil.isNull(loginPicture)) {
                return new ArrayList<>();
            }

            //当spaceId为null时,代表为公共图库
            //只有所有人和管理员有所有权限, 其他人只能查看
            spaceId = loginPicture.getSpaceId();
            if (ObjUtil.isNull(spaceId)) {
                if (userService.isAdmin(loginUser) || loginUserId.equals(loginPicture.getUserId())) {
                    return ADMIN_PERMISSIONS;
                } else {//只有查看的权限 Collections.singletonList 创建一个不可变的单元素列表
                    return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }

        // 根据space判断
        //获取space信息
        Space space = spaceService.getById(spaceId);
        //判断space的类型 ,是团队空间还是个人空间
        //私人空间
        if (space.getSpaceType().equals(SpaceTypeEnum.PRIVATE.getValue())) {
            //只允许本人和管理员可以访问
            //否则返回空
            if (userService.isAdmin(loginUser) || loginUserId.equals(space.getUserId())) {
                return ADMIN_PERMISSIONS;
            } else {
                return new ArrayList<>();
            }
        } else { //此时为团队空间
            //查询spaceUser表
            spaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceId)
                    .eq(SpaceUser::getUserId, loginUserId)
                    .one();

            //当为空时, 返回空权限
            if (ObjUtil.isNull(spaceUser)) {
                return new ArrayList<>();
            }
            //根据用户角色返回对应权限
            return spaceUserAuthManger.getPermissionsByRole(spaceUser.getSpaceRole());
        }
    }


    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     * 本项目不使用角色控制, 直接返回null
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {

        return null;
    }

    /**
     * 从请求中获取上下文对象
     */
    private SpaceUserAuthContext getAuthContextByRequest() {
        //获取请求对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        //获取请求方式
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext authContext;
        //获取参数
        //如果是json格式的请求
        if (ContentType.JSON.getValue().equals(contentType)) {
            //获取请求参数
            String body = ServletUtil.getBody(request);
            authContext = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {//如果是get请求
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authContext = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }

        //g根据请求路径区分 id 字段的涵义
        //获取id
        Long id = authContext.getId();
        if (ObjUtil.isNotNull(id)) {
            //获取请求路径的业务前缀
            String requestURI = request.getRequestURI();
            //获取业务前缀
            //替换掉上下文 /api, 剩下的就是前缀
            //将/api替换为空
            String partURI = requestURI.replace(contextPath + "/", "");
            //获取前缀的第一个斜杠前的字符串
            String moduleName = StrUtil.subBefore(partURI, "/", false);
            //根据不同的模块, 设置不同的id
            switch (moduleName) {
                case "picture":
                    authContext.setPictureId(id);
                    break;
                case "spaceUser":
                    authContext.setSpaceUserId(id);
                    break;
                case "space":
                    authContext.setSpaceId(id);
                    break;
                default:
            }
        }
        return authContext;
    }

    /**
     * 判断所有字段是否为空
     *
     * @param object
     * @return true 为空 , false 不为空
     */
    private boolean isAllFieldNull(Object object) {
        if (ObjUtil.isNull(object)) {
            return true; //本身为空
        }

        return Arrays.stream(ReflectUtil.getFields(object.getClass()))//获取所有字段
                .map(field -> ReflectUtil.getFieldValue(object, field))//获取字段值
                .allMatch(ObjUtil::isEmpty);//判断是否为空
    }
}
