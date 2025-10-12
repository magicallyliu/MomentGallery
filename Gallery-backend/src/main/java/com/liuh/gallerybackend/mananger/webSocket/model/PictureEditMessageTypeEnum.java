package com.liuh.gallerybackend.mananger.webSocket.model;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @Author LiuH
 * @Date 2025/10/12 下午1:37
 * @PackageName com.liuh.gallerybackend.mananger.webSocket.model
 * @ClassName PictureEditMessageTypeEnum
 * @Version
 * @Description 图片编辑消息枚举
 */

@SuppressWarnings("all")
@Getter
public enum PictureEditMessageTypeEnum {

    INFO("发送通知", "INFO"),
    ERROR("发送错误", "ERROR"),
    ENTER_EDIT("进入编辑状态", "ENTER_EDIT"),
    EXIT_EDIT("退出编辑状态", "EXIT_EDIT"),
    EDIT_ACTION("执行编辑操作", "EDIT_ACTION");

    private final String text;
    private final String value;

    PictureEditMessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static PictureEditMessageTypeEnum getEnumByValue(String value) {
        if (StrUtil.isEmpty(value) || value.isEmpty()) {
            return null;
        }
        for (PictureEditMessageTypeEnum typeEnum : PictureEditMessageTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }
}
