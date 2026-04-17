package com.jcen.medpal.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 管理员重置用户密码请求
 */
@Data
public class UserAdminPasswordResetRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 id
     */
    private Long id;
}
