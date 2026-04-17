package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.service.BaseService;
import java.util.List;

/**
 * 通用服务基类实现
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
public abstract class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<T> {

    @Override
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return baseMapper.deleteBatchIds(ids);
    }

    @Override
    public T getOne(QueryWrapper<T> queryWrapper) {
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean exists(QueryWrapper<T> queryWrapper) {
        return baseMapper.selectCount(queryWrapper) > 0;
    }
}
