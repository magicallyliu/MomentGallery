package com.liuh.gallerybackend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author LiuH
 * @Date 2025/5/7 下午9:13
 * @PackageName com.liuh.gallerybackend.model.vo
 * @ClassName LoginUserVO
 * @Version
 * @Description 脱敏后的用户登录数据
 */

@SuppressWarnings("all")
@Data
public class LoginUserVO implements Serializable {
    /**
     * id
     * 使用 ASSIGN_ID 生成一个较长的无序ID(使用雪花算法)
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
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    private static final long serialVersionUID = 1L;
}
