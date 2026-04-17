package com.jcen.medpal.controller;

import com.jcen.medpal.common.BaseResponse;
import com.jcen.medpal.common.DeleteRequest;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.exception.BusinessException;

/**
 * 通用 CRUD 控制器基类
 * 
 * 注：由于 Java 泛型限制，建议直接在具体 Controller 中实现 CRUD 操作
 * 参考 UserController 的实现方式
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
public abstract class BaseController {

    /**
     * 删除
     */
    protected BaseResponse<Boolean> delete(DeleteRequest request) {
        if (request == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 批量删除
     */
    protected BaseResponse<Integer> deleteBatch(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(ids.size());
    }
}
