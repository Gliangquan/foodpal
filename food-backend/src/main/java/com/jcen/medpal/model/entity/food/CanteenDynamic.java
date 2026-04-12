package com.jcen.medpal.model.entity.food;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("canteen_dynamic")
public class CanteenDynamic implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long publisherId;

    private String publisherType;

    private Long merchantId;

    private String title;

    private String content;

    private String coverImage;

    private String mediaUrls;

    private String newsType;

    private String auditStatus;

    /**
     * 兼容旧库：部分环境未建 audit_reason 列，先作为非持久化字段避免 SQL 报错。
     */
    @TableField(exist = false)
    private String auditReason;

    private String status;

    private LocalDateTime expireTime;

    /**
     * 仅用于列表展示，不落库。
     */
    @TableField(exist = false)
    private String merchantName;

    private Integer likeCount;

    private Integer commentCount;

    private Integer shareCount;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
