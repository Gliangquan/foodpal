package com.jcen.medpal.model.entity.food;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("canteen_announcement")
public class CanteenAnnouncement implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String coverImage;

    private String announcementType;

    private LocalDateTime expireTime;

    private LocalDateTime publishTime;

    private Integer isTop;

    private Integer sortNo;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
