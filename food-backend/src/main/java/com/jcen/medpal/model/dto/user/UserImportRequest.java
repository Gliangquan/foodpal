package com.jcen.medpal.model.dto.user;

import lombok.Data;

/**
 * 用户导入请求
 */
@Data
public class UserImportRequest {
    /**
     * 文件URL或Base64编码的文件内容
     */
    private String fileContent;

    /**
     * 文件类型：csv 或 excel
     */
    private String fileType;

    /**
     * 是否跳过重复数据
     */
    private Boolean skipDuplicate = true;

    /**
     * 默认密码
     */
    private String defaultPassword = "123456";

    /**
     * 默认角色
     */
    private String defaultRole = "user";
}
