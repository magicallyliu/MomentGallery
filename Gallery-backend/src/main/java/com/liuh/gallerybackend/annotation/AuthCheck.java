package com.liuh.gallerybackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author LiuH
 * @Date 2025/5/12 下午9:12
 * @PackageName com.liuh.gallerybackend.annotation
 * @ClassName AuthCheck
 * @Version
 * @Description 权限效验注解,  用户必须登录才可以调用
 */

@SuppressWarnings("all")
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须具有某个角色
     */
    String mustRole() default "";
}
