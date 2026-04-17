package com.jcen.medpal.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户登录请求
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 登录类型：account（账号登录）或 phone（手机号登录）
     */
    private String loginType;

    private String userAccount;

    private String userPhone;

    private String userPassword;
}
