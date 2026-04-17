package com.jcen.medpal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String type;
    private String title;
    private String content;
    private String relatedType;
    private Long relatedId;
    private String status;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
    
    private static final long serialVersionUID = 1L;
}
