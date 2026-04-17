package com.jcen.medpal.model.dto.user;

import lombok.Data;

/**
 * 用户导出请求
 */
@Data
public class UserExportRequest {
    /**
     * 导出格式：csv、excel、json
     */
    private String format = "excel";

    /**
     * 搜索条件 - 用户昵称
     */
    private String userName;

    /**
     * 搜索条件 - 用户账号
     */
    private String userAccount;

    /**
     * 搜索条件 - 用户角色
     */
    private String userRole;

    /**
     * 搜索条件 - 账户状态
     */
    private Integer status;

    /**
     * 是否导出所有字段
     */
    private Boolean exportAll = false;

    /**
     * 自定义导出字段（逗号分隔）
     */
    private String fields;
}
