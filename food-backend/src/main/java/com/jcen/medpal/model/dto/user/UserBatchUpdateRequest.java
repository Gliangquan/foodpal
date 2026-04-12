package com.jcen.medpal.model.dto.user;

import lombok.Data;
import java.util.List;

/**
 * 批量更新用户请求
 */
@Data
public class UserBatchUpdateRequest {
    /**
     * 用户ID列表
     */
    private List<Long> ids;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 账户状态
     */
    private Integer status;

    /**
     * 用户简介
     */
    private String userProfile;
}
