package com.liuh.gallerybackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Map;

/**
 * @Author LiuH
 * @Date 2025/5/6 下午5:30
 * @PackageName com.liuh.gallerybackend.model.enums
 * @ClassName UserRoleEnum
 * @Version
 * @Description 用户角色枚举
 */

@SuppressWarnings("all")
@Getter
public enum UserRoleEnum {
    USER("用户","user"),
    ADMIN("管理员","admin");

    /**
     * 角色枚举类的名称
     */
    private final String text;

    /**
     * 角色枚举类的值
     */
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据值返回枚举
     * @param value
     * @return 找到则返回枚举, 否则返回null
     */
    public static UserRoleEnum getEnumByValue(String value) {
        //判断是否为空
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }

        //获取角色枚举的名称
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if(userRoleEnum.getValue().equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}
