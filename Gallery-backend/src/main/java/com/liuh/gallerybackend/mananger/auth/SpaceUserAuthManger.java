package com.liuh.gallerybackend.mananger.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.liuh.gallerybackend.mananger.auth.model.SpaceUserAuthConfig;
import com.liuh.gallerybackend.mananger.auth.model.SpaceUserPermissionConstant;
import com.liuh.gallerybackend.mananger.auth.model.SpaceUserRole;
import com.liuh.gallerybackend.model.entity.Space;
import com.liuh.gallerybackend.model.entity.SpaceUser;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.enums.SpaceRoleEnum;
import com.liuh.gallerybackend.model.enums.SpaceTypeEnum;
import com.liuh.gallerybackend.service.SpaceUserService;
import com.liuh.gallerybackend.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author LiuH
 * @Date 2025/10/9 下午7:50
 * @PackageName com.liuh.gallerybackend.mananger.auth
 * @ClassName SpaceUserAuthManger
 * @Version
 * @Description 空间成员权限管理 --全局配置
 */

@SuppressWarnings("all")
@Component
public class SpaceUserAuthManger {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService  spaceUserService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG ;

    static {
        //使用hutool读取配置文件
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        //将json字符串转为对象
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色查询权限配置
     *  @param spaceUserRole 需要查询权限的角色
     * @return
     */
    public List<String> getPermissionsByRole(String spaceUserRole) {
        if (StrUtil.isBlank(spaceUserRole)){
            return new ArrayList<>();
        }

        SpaceUserRole userRole = SPACE_USER_AUTH_CONFIG.getRoles()
                .stream()
                .filter(role -> spaceUserRole.equals(role.getKey()))//过滤出与角色匹配的元素
                .findFirst()//返回流中的第一个元素（如果流非空
                .orElse(null);

        //如果userRole为空, 返回空
        if (ObjUtil.isNull(userRole)){
            return new ArrayList<>();
        }
        //返回权限列表
        return userRole.getPermissions();
    }

    /**
     * 根据空间对象获取权限列表
     */
    public List<String> getPermissionList(Space space, User loginUser){
        //判断登录用户是否存在
        //不存在则无权限
        if (ObjUtil.isNull(loginUser)){
            return new ArrayList<>();
        }

        //管理员权限
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        //是否为公共图库
        if (ObjUtil.isNull(space)){
            //管理员则返回管理员权限
            if (userService.isAdmin(loginUser)){
                return ADMIN_PERMISSIONS;
            }
            //否则返回只读权限
            return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
        }

        //判断空间类别
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        //为空返回空权限
        if (ObjUtil.isNull(spaceTypeEnum)){
            return new ArrayList<>();
        }
        //如果是私有空间, 只有管理员和空间所有者返回管理员权限, 其他人返回空权限
        if (SpaceTypeEnum.PRIVATE.equals(spaceTypeEnum)){
            if (userService.isAdmin(loginUser) || space.getUserId().equals(loginUser.getId())){
                return ADMIN_PERMISSIONS;
            }
            return new ArrayList<>();
        }else if (SpaceTypeEnum.TEAM.equals(spaceTypeEnum)){
            //如果是公共空间, 查询在团队中等等权限
            SpaceUser spaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, space.getId())
                    .eq(SpaceUser::getUserId, loginUser.getId())
                    .one();
            //不存在返回空
            if (ObjUtil.isNull(spaceUser)){
                return new ArrayList<>();
            }
            //更具在空间的角色返回权限
            return  getPermissionsByRole(spaceUser.getSpaceRole());
        }
        //什么都不是
        return new ArrayList<>();
    }
}
