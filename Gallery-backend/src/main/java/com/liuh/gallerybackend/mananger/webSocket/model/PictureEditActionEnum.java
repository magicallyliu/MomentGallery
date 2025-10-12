package com.liuh.gallerybackend.mananger.webSocket.model;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @Author LiuH
 * @Date 2025/10/12 下午1:39
 * @PackageName com.liuh.gallerybackend.mananger.webSocket.model
 * @ClassName PictureEditActionEnum
 * @Version
 * @Description 图片编辑操作枚举
 */

@SuppressWarnings("all")
@Getter
public enum PictureEditActionEnum {

    ZOOM_IN("放大操作", "ZOOM_IN"),
    ZOOM_OUT("缩小操作", "ZOOM_OUT"),
    ROTATE_LEFT("左旋操作", "ROTATE_LEFT"),
    ROTATE_RIGHT("右旋操作", "ROTATE_RIGHT");

    private final String text;
    private final String value;

    PictureEditActionEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static PictureEditActionEnum getEnumByValue(String value) {
        if (StrUtil.isEmpty(value) || value.isEmpty()) {
            return null;
        }
        for (PictureEditActionEnum actionEnum : PictureEditActionEnum.values()) {
            if (actionEnum.value.equals(value)) {
                return actionEnum;
            }
        }
        return null;
    }
}
