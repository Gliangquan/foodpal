package com.jcen.medpal.model.entity.food;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("merchant_profile")
public class MerchantProfile implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String merchantName;

    private String contactName;

    private String contactPhone;

    private String avatar;

    private String businessHours;

    private String location;

    private String description;

    private Integer status;

    private String auditStatus;

    /**
     * 待审核变更(JSON)
     */
    private String pendingData;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
