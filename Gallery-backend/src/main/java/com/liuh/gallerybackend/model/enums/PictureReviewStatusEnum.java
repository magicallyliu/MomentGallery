package com.liuh.gallerybackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @Author LiuH
 * @Date 2025/9/19 下午3:52
 * @PackageName com.liuh.gallerybackend.model.enums
 * @ClassName PictureReviewStatusEnum
 * @Version
 * @Description 用于图片审核
 */

@SuppressWarnings("all")
@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    /**
     * 审核的状态
     */
    private final String text;
    /**
     * 审核状态对应的数字
     */
    private final Integer value;

    PictureReviewStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据值返回枚举
     *
     * @param value 审核状态对应的数字
     * @return
     */
    public static PictureReviewStatusEnum getEnum(Integer value) {
        //判断是否为空
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum e : PictureReviewStatusEnum.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }
}
