package com.jcen.medpal.model.entity.food;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("canteen_dynamic_share")
public class CanteenDynamicShare implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dynamicId;

    private Long userId;

    private String shareChannel;

    private LocalDateTime createTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
