package com.liuh.gallerybackend.mananger.webSocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.liuh.gallerybackend.mananger.disruptor.PictureEditEventProducer;
import com.liuh.gallerybackend.mananger.webSocket.model.PictureEditActionEnum;
import com.liuh.gallerybackend.mananger.webSocket.model.PictureEditMessageTypeEnum;
import com.liuh.gallerybackend.mananger.webSocket.model.PictureEditRequestMessage;
import com.liuh.gallerybackend.mananger.webSocket.model.PictureEditResponseMessage;
import com.liuh.gallerybackend.model.dto.picture.PictureEditRequest;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LiuH
 * @Date 2025/10/12 下午2:13
 * @PackageName com.liuh.gallerybackend.mananger.webSocket
 * @ClassName PictureEditHandler
 * @Version
 * @Description 图片编辑处理器
 */

@Log4j2
@SuppressWarnings("all")
@Component
public class PictureEditHandler extends TextWebSocketHandler {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private PictureEditEventProducer pictureEditEventProducer;
    /**
     * 每张图片的编辑状态，key: pictureId, value: 当前正在编辑的用户 ID
     */
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    /**
     * 保存所有连接的会话，key: pictureId, value: 用户会话集合
     */
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    /**
     * 连接建立后
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        //保存会话都集合中
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        //初始化集合 //如果不存在, 执行初始化操作
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        //将当前会话添加到集合中
        pictureSessions.get(pictureId).add(session);

        //构造响应, 发送加入编辑的消息通知
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        //发送通知
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String format = String.format("用户 %s 加入了编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(format);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));

        //广播消息
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    /**
     * 接收到消息 , 根据消息类别处理消息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        //获取消息, 将message  转为字符串
        //message.getPayload() 获取消息内容
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);


        //获取公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");

        //根据消息类型处理消息
        //使用disruptor 优化
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage, session, user, pictureId);
        //使用disruptor 优化
//        String type = pictureEditRequestMessage.getType();
//        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.getEnumByValue(type);
//        switch (pictureEditMessageTypeEnum) {
//            case ENTER_EDIT://进入编辑状态
//                handleEnterEditMessage(pictureEditRequestMessage, session, user, pictureId);
//                break;
//            case EXIT_EDIT: //退出编辑状态
//                handleExitEditMessage(pictureEditRequestMessage, session, user, pictureId);
//                break;
//            case EDIT_ACTION: //执行编辑操作
//                handleEditActionMessage(pictureEditRequestMessage, session, user, pictureId);
//                break;
//            default:
//                //其他消息类型, 暂时不处理
//                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
//                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
//                pictureEditResponseMessage.setMessage("不支持的消息类型");
//                pictureEditResponseMessage.setUser(userService.getUserVO(user));
//
//                //配置序列化, 解决精度丢失问题
//                ObjectMapper objectMapper = new ObjectMapper();
//                SimpleModule module = new SimpleModule();
//                module.addSerializer(Long.class, ToStringSerializer.instance);
//                module.addSerializer(Long.TYPE, ToStringSerializer.instance);
//                objectMapper.registerModule(module);
//
//                //将对象转换为 json 字符串 再转换为 TextMessage 对象
//                TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(pictureEditResponseMessage));
//
//                //发送广播
//                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pictureEditResponseMessage)));
//                break;
//        }

    }


    /**
     * 进入编辑状态
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        //只有在没有用户在编辑图片的状态下, 才能编辑图片
        //判断是否在编辑表中, 不在才能编辑
        if (!pictureEditingUsers.containsKey(pictureId)) {
            //设置当前图片在编辑中
            pictureEditingUsers.put(pictureId, user.getId());
            //构造响应, 发送加入编辑的消息通知
            //构造响应, 发送加入编辑的消息通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            //发送通知
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String format = String.format("用户 %s 开始了编辑", user.getUserName());
            pictureEditResponseMessage.setMessage(format);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));

            //广播消息
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }


    /**
     * 退出编辑状态
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        //获取信息 -- 正在编辑的用户
        Long editUserId = pictureEditingUsers.get(pictureId);

        if (ObjUtil.isNotNull(editUserId) && editUserId.equals(user.getId())) {
            //移除用户正在编辑的状态
            pictureEditingUsers.remove(pictureId);
            //构造响应, 发送退出编辑的消息通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            //发送通知
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String format = String.format("用户 %s 退出了编辑", user.getUserName());
            pictureEditResponseMessage.setMessage(format);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));

            //广播消息
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }

    }

    /**
     * 执行编辑操作
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        //获取信息 -- 正在编辑的用户
        Long editUserId = pictureEditingUsers.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        //获取编辑操作
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (ObjUtil.isNull(pictureEditActionEnum)) {
            log.info("不支持的编辑操作");
            return;
        }
        //判断是否是当前正在编辑的用户
        if (ObjUtil.isNotNull(editUserId) && editUserId.equals(user.getId())) {
            //发送具体的操作通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            //发送通知
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String format = String.format("用户 %s 执行 %s", user.getUserName(), pictureEditActionEnum.getText());
            pictureEditResponseMessage.setMessage(format);
            pictureEditResponseMessage.setEditAction(editAction);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));

            //广播消息 -- 除了当前用户之外等等其他用户
            broadcastToPicture(pictureId, pictureEditResponseMessage, session);

        }
    }

    /**
     * 连接关闭后
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        handleExitEditMessage(null, session, user, pictureId);

        //移除会话
        Set<WebSocketSession> sessions = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessions)) {
            sessions.remove(session);
            //如果会话中的用户全部离开
            //移除会话
            if (sessions.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }
        //通知其他用户, 该用户退出了编辑
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        //发送通知
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String format = String.format("用户 %s 离开了编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(format);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));

        //广播消息
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    /**
     * 广播的方法, 用于向该图片的用户传递信息
     * 不会发送到本身的会话, 防止接收到广播二次操作
     *
     * @param pictureId
     * @param pictureEditResponseMessage
     * @param excludeSession             本身的会话, 防止接收到广播二次操作
     * @throws IOException
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws IOException {
        //获取该图片的会话集合
        Set<WebSocketSession> sessions = pictureSessions.get(pictureId);
        //判断是否为空
        if (CollUtil.isNotEmpty(sessions)) {
            //配置序列化, 解决精度丢失问题
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(module);

            //将对象转换为 json 字符串 再转换为 TextMessage 对象
            TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(pictureEditResponseMessage));
            //遍历会话集合
            for (WebSocketSession session : sessions) {
                //排除自身
                if (ObjUtil.isNotNull(excludeSession) && session.equals(excludeSession)) {
                    continue;
                }
                //判断session 是否为打开状态
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    /**
     * 广播的方法, 用于向该图片的用户传递信息
     *
     * @param pictureId
     * @param pictureEditResponseMessage
     * @throws IOException
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws IOException {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }
}
