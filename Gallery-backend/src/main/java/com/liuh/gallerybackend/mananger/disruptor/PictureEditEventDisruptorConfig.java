package com.liuh.gallerybackend.mananger.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @Author LiuH
 * @Date 2025/10/12 下午4:42
 * @PackageName com.liuh.gallerybackend.mananger.disruptor
 * @ClassName PictureEditEventDisruptorConfig
 * @Version
 * @Description 事件处理配置类
 */

@SuppressWarnings("all")
@Configuration
public class PictureEditEventDisruptorConfig {

    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBuffer() {
        //环形缓冲器的大小
        int bufferSize = 1024 * 256;
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(
                PictureEditEvent::new, // 事件工厂类，用于创建事件对象, 放到缓冲区的数据类型
                bufferSize,
                ThreadFactoryBuilder.create()//线程池，用于处理事件
                        .setNamePrefix("pictureEditEventDisruptor")//setNamePrefix("pictureEditEventDisruptor")  设置线程池名称前缀
                        .build()

        );
        // 设置消费者
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        // 开启 disruptor
        disruptor.start();
        return disruptor;
    }
}
