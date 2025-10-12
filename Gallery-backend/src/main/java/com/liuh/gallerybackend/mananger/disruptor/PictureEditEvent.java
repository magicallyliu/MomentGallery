package com.liuh.gallerybackend.mananger.disruptor;

import com.liuh.gallerybackend.mananger.webSocket.model.PictureEditRequestMessage;
import com.liuh.gallerybackend.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * @Author LiuH
 * @Date 2025/10/12 下午4:34
 * @PackageName com.liuh.gallerybackend.mananger.disruptor
 * @ClassName PictureEditEvent
 * @Version
 * @Description 图片编辑事件 -- disruptor
 * 对应图片处理的参数
 */

@SuppressWarnings("all")
@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片 id
     */
    private Long pictureId;

}
