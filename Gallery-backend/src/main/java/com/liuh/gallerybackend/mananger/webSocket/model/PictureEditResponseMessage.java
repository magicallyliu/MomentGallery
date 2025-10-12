package com.liuh.gallerybackend.mananger.webSocket.model;

import com.liuh.gallerybackend.model.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author LiuH
 * @Date 2025/10/12 下午1:35
 * @PackageName com.liuh.gallerybackend.mananger.webSocket.model
 * @ClassName PictureEditResponseMessage
 * @Version
 * @Description 图片编辑响应消息
 */

@SuppressWarnings("all")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditResponseMessage {

    /**
     * 消息类型，例如 "INFO", "ERROR", "ENTER_EDIT", "EXIT_EDIT", "EDIT_ACTION"
     */
    private String type;

    /**
     * 信息
     */
    private String message;

    /**
     * 执行的编辑动作 执行的编辑动作 例如: 放大, 缩小, 左旋, 右旋
     */
    private String editAction;

    /**
     * 用户信息
     */
    private UserVO user;
}
