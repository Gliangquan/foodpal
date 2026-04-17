package com.jcen.medpal.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新请求
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

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
     * 用户角色：student/admin/ban
     */
    private String userRole;

    /**
     * 账户状态：1启用 0禁用
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
