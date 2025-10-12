package com.liuh.gallerybackend.mananger.disruptor;

import com.liuh.gallerybackend.mananger.webSocket.model.PictureEditRequestMessage;
import com.liuh.gallerybackend.mananger.webSocket.model.PictureEditResponseMessage;
import com.liuh.gallerybackend.model.dto.picture.PictureEditRequest;
import com.liuh.gallerybackend.model.entity.User;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * @Author LiuH
 * @Date 2025/10/12 下午4:46
 * @PackageName com.liuh.gallerybackend.mananger.disruptor
 * @ClassName PictureEditEventProducer
 * @Version
 * @Description 图片编辑事件的生产者
 */

@SuppressWarnings("all")
@Component
@Slf4j
public class PictureEditEventProducer {
    @Resource
    private Disruptor<PictureEditEvent> disruptor;

    /**
     * 发布事件的方法
     *  将事件放入环形缓冲区中
     * @param pictureEditResponseMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void publishEvent(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) {
        //获取环形缓冲区中可以放置数据的位置
        RingBuffer<PictureEditEvent> ringBuffer = disruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        PictureEditEvent pictureEditEvent = ringBuffer.get(sequence);
        //给事件对象赋值
        pictureEditEvent.setPictureEditRequestMessage(pictureEditRequestMessage);
        pictureEditEvent.setSession(session);
        pictureEditEvent.setUser(user);
        pictureEditEvent.setPictureId(pictureId);
        //发布事件
        ringBuffer.publish(sequence);
    }

    /**
     * 优雅停机
     * 当关闭应用后, 会将环形缓冲区中的数据执行完成
     */
    @PreDestroy
    public void destroy(){
        disruptor.shutdown();
    }
}
