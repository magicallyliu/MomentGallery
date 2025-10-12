package com.liuh.gallerybackend.mananger.disruptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.liuh.gallerybackend.mananger.webSocket.PictureEditHandler;
import com.liuh.gallerybackend.mananger.webSocket.model.PictureEditMessageTypeEnum;
import com.liuh.gallerybackend.mananger.webSocket.model.PictureEditRequestMessage;
import com.liuh.gallerybackend.mananger.webSocket.model.PictureEditResponseMessage;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.service.UserService;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

/**
 * @Author LiuH
 * @Date 2025/10/12 下午4:36
 * @PackageName com.liuh.gallerybackend.mananger.disruptor
 * @ClassName PictureEditEventWorkHandler
 * @Version
 * @Description 定义事件处理器(消费者)
 */

@SuppressWarnings("all")
@Component
@Slf4j
public class PictureEditEventWorkHandler implements WorkHandler<PictureEditEvent> {

    @Resource
    private PictureEditHandler pictureEditHandler;

    @Resource
    private UserService userService;

    /**
     * 接收到事件后的处理
     *
     * @param pictureEditEvent
     * @throws Exception
     */
    @Override
    public void onEvent(PictureEditEvent pictureEditEvent) throws Exception {
        //获取事件对象
        PictureEditRequestMessage pictureEditRequestMessage = pictureEditEvent.getPictureEditRequestMessage();
        WebSocketSession session = pictureEditEvent.getSession();
        User user = pictureEditEvent.getUser();
        Long pictureId = pictureEditEvent.getPictureId();
        //获取消息类型
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.getEnumByValue(pictureEditRequestMessage.getType());
        switch (pictureEditMessageTypeEnum) {
            case ENTER_EDIT://进入编辑状态
                pictureEditHandler.handleEnterEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EXIT_EDIT: //退出编辑状态
                pictureEditHandler.handleExitEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EDIT_ACTION: //执行编辑操作
                pictureEditHandler.handleEditActionMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            default:
                //其他消息类型, 暂时不处理
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
                pictureEditResponseMessage.setMessage("不支持的消息类型");
                pictureEditResponseMessage.setUser(userService.getUserVO(user));

                //配置序列化, 解决精度丢失问题
                ObjectMapper objectMapper = new ObjectMapper();
                SimpleModule module = new SimpleModule();
                module.addSerializer(Long.class, ToStringSerializer.instance);
                module.addSerializer(Long.TYPE, ToStringSerializer.instance);
                objectMapper.registerModule(module);

                //将对象转换为 json 字符串 再转换为 TextMessage 对象
                TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(pictureEditResponseMessage));

                //发送广播
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pictureEditResponseMessage)));
                break;
        }
    }
}
