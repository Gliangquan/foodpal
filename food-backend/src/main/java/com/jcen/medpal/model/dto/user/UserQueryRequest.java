package com.jcen.medpal.model.dto.user;

import com.jcen.medpal.common.PageRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询请求
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 开放平台id
     */
    private String unionId;

    /**
     * 公众号openId
     */
    private String mpOpenId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 实名认证状态
     */
    private String realNameStatus;

    /**
     * 资质认证状态
     */
    private String qualificationStatus;

    /**
     * 关键字（昵称/账号/手机号）
     */
    private String keyword;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 账号状态：1启用 0禁用
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
