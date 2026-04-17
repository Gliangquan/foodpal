package com.jcen.medpal.model.vo;

import java.util.Date;
import lombok.Data;

/**
 * 通用视图对象基类
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
public class BaseVO {

    /**
     * id
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
