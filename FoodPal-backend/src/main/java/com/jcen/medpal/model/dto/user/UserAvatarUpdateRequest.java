package com.jcen.medpal.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户头像更新请求
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
@ApiModel(value = "用户头像更新请求", description = "用户更新头像时的请求参数")
public class UserAvatarUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "头像URL", required = true)
    private String avatarUrl;
}