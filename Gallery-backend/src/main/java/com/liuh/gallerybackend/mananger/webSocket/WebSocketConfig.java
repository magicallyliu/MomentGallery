package com.liuh.gallerybackend.mananger.webSocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

/**
 * @Author LiuH
 * @Date 2025/10/12 下午4:00
 * @PackageName com.liuh.gallerybackend.mananger.webSocket
 * @ClassName WebSocketConfig
 * @Version
 * @Description WebSocket配置 (定义连接)
 */

@SuppressWarnings("all")
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private PictureEditHandler pictureEditHandler;

    @Resource
    private WsHandshakelnterceptor wsHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // websocket
        registry.addHandler(pictureEditHandler, "/ws/picture/edit")//处理器 请求地址
                .addInterceptors(wsHandshakeInterceptor)//拦截器
                .setAllowedOrigins("*");
    }
}
