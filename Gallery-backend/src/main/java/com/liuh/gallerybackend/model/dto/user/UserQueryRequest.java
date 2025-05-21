package com.liuh.gallerybackend.model.dto.user;

import com.liuh.gallerybackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author LiuH
 * @Date 2025/5/13 下午9:33
 * @PackageName com.liuh.gallerybackend.model.dto.user
 * @ClassName as
 * @Version
 * @Description 用户查询请求
 */

//@EqualsAndHashCode注解的callSuper参数的作用是控制生成的equals和hashCode方法是否调用父类的相应方法。
// 如果设置为true，那么在生成的方法中会调用super.equals()和super.hashCode()，
// 这样就会将父类的字段也纳入比较和哈希计算中。如果设置为false（默认值），
// 则仅基于当前类中定义的字段来生成方法，忽略父类的字段。
//
//举个例子，假设有一个父类Parent，有字段id，子类Child继承Parent，并添加字段name。
// 如果Child类使用了@EqualsAndHashCode(callSuper = true)，那么生成的equals方法会比较id和name两个字段，
// 而如果callSuper为false，则只比较name字段，忽略父类的id。
// 这种情况下，可能会导致两个不同id的子类实例被错误地认为是相等的，如果它们的name相同的话，这显然有问题。
@SuppressWarnings("all")
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
