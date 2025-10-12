package com.liuh.gallerybackend.mananger.webSocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.liuh.gallerybackend.mananger.auth.SpaceUserAuthManger;
import com.liuh.gallerybackend.mananger.auth.model.SpaceUserPermissionConstant;
import com.liuh.gallerybackend.model.entity.Picture;
import com.liuh.gallerybackend.model.entity.Space;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.enums.SpaceTypeEnum;
import com.liuh.gallerybackend.service.PictureService;
import com.liuh.gallerybackend.service.SpaceService;
import com.liuh.gallerybackend.service.SpaceUserService;
import com.liuh.gallerybackend.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author LiuH
 * @Date 2025/10/12 下午1:43
 * @PackageName com.liuh.gallerybackend.mananger.webSocket
 * @ClassName WsHandshakelnterceptor
 * @Version
 * @Description wenSocket握手拦截器
 */

@Log4j2
@SuppressWarnings("all")
@Component
public class WsHandshakelnterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private SpaceUserAuthManger spaceUserAuthManger;

    /**
     * 握手之前执行该方法，给服务器添加信息，传递给WebSocketHandler
     * <p>
     * 进行权限效验, 如果该用户在在团队空间中没有编辑的权限, 则不允许进入
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes 给 webSocketSession 会话设置属性
     *                   attributes.put("user", loginUser);
     *                   attributes.put("userId",  loginUser.getId());
     *                   attributes.put("pictureId", Long.valueOf(pictureId)); //需要将String ->  Long
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        //获取当前登录用户
        //如果 request 属于 ServletServerHttpResponse 则转换 HttpServletResponse
        if (response instanceof ServletServerHttpResponse) {
            //转换
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

            //从请求中获取参数
            String pictureId = servletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.error("缺少图片参数, 拒绝握手");
                return false;
            }

            //获取用户信息
            User loginUser = userService.getLoginUser(servletRequest);

            //效验是否有编辑权限
            Picture picture = pictureService.getById(pictureId);
            if (ObjUtil.isNull(picture)) {
                log.error("图片不存在, 拒绝握手");
                return false;
            }
            //获取图片所属空间
            Long spaceId = picture.getSpaceId();
            Space space = null;
            //不为公共空间
            if (ObjUtil.isNotNull(spaceId)) {
                space = spaceService.getById(spaceId);
                if (ObjUtil.isNull(space)) {
                    log.error("空间不存在, 拒绝握手");
                    return false;
                }

                //判断是否为团队空间
                if (!space.getSpaceType().equals(SpaceTypeEnum.TEAM.getValue())) {
                    log.error("不是团队空间, 拒绝握手");
                    return false;
                }

            }

            //是团队空间, 并且有编辑者的权限, 才能建立连接
            //获取用户的权限列表
            List<String> permissionList = spaceUserAuthManger.getPermissionList(space, loginUser);
            //判断是否包含编辑权限
            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                log.error("没有编辑权限, 拒绝握手");
                return false;
            }

            //设置用户信息到 webSocket 会话中
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId)); //需要将String ->  Long
        }
        return true;
    }

    /**
     * 握手之后执行该方法，给WebSocketHandler传递信息
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param exception
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler
            wsHandler, Exception exception) {

    }
}
