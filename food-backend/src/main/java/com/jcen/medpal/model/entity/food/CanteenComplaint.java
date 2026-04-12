package com.jcen.medpal.model.entity.food;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("canteen_complaint")
public class CanteenComplaint implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String complaintNo;

    private Long userId;

    private Long merchantId;

    private String complaintTitle;

    private String complaintContent;

    private String evidenceUrls;

    private String status;

    private String processProgress;

    private String rectifyRequirement;

    private String rectifyResult;

    private String feedback;

    private Integer resultRating;

    private Long processBy;

    private LocalDateTime processTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
