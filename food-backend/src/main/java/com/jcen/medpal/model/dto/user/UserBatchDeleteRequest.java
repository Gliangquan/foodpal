package com.jcen.medpal.model.dto.user;

import lombok.Data;
import java.util.List;

/**
 * 批量删除用户请求
 */
@Data
public class UserBatchDeleteRequest {
    /**
     * 用户ID列表
     */
    private List<Long> ids;

    /**
     * 是否软删除（true=软删除，false=硬删除）
     */
    private Boolean softDelete = true;
}
