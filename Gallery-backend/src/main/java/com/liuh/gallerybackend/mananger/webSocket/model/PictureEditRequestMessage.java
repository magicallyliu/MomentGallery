package com.liuh.gallerybackend.mananger.webSocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author LiuH
 * @Date 2025/10/12 下午1:32
 * @PackageName com.liuh.gallerybackend.mananger.webSocket.model
 * @ClassName PictureEditRequestMessage
 * @Version
 * @Description 客户端向服务器发送的图片编辑请求
 */

@SuppressWarnings("all")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditRequestMessage {

    /**
     * 消息类型，例如 "ENTER_EDIT", "EXIT_EDIT", "EDIT_ACTION"
     */
    private String type;

    /**
     * 执行的编辑动作 例如: 放大, 缩小, 左旋, 右旋
     */
    private String editAction;
}
