package com.jcen.medpal.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新个人信息请求
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 性别
     */
    private String gender;

    /**
     * 年龄
     */
    private Integer age;

    private static final long serialVersionUID = 1L;
}