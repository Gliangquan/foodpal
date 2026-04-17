package com.jcen.medpal.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户创建请求
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色: student/admin
     */
    private String userRole;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 账号状态
     */
    private Integer status;

    /**
     * 用户简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}
