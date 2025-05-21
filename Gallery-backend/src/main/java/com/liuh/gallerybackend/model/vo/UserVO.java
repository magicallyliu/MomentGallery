package com.liuh.gallerybackend.model.vo;



import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author LiuH
 * @Date 2025/5/13 下午9:44
 * @PackageName com.liuh.gallerybackend.model.vo
 * @ClassName UserVO
 * @Version
 * @Description 脱敏后的用户信息
 */

@SuppressWarnings("all")
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
