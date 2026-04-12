package com.jcen.medpal.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 通用服务基类接口
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
public interface BaseService<T> extends IService<T> {

    /**
     * 批量删除
     *
     * @param ids 主键列表
     * @return 删除数量
     */
    int deleteBatch(List<Long> ids);

    /**
     * 根据条件查询单条记录
     *
     * @param queryWrapper 查询条件
     * @return 记录
     */
    T getOne(QueryWrapper<T> queryWrapper);

    /**
     * 检查记录是否存在
     *
     * @param queryWrapper 查询条件
     * @return 是否存在
     */
    boolean exists(QueryWrapper<T> queryWrapper);
}
