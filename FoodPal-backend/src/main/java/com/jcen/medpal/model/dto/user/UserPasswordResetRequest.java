package com.jcen.medpal.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户找回密码请求
 */
@Data
public class UserPasswordResetRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 绑定手机号
     */
    private String userPhone;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认新密码
     */
    private String checkPassword;
}
