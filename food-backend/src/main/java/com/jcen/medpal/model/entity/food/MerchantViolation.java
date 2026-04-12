package com.jcen.medpal.model.entity.food;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("merchant_violation")
public class MerchantViolation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private String violationType;

    private String violationReason;

    private String penaltyMeasures;

    private String violationLevel;

    private Long supervisorId;

    private String processStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}