package com.jcen.medpal.model.entity.food;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dish")
public class Dish implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private String dishName;

    private BigDecimal dishPrice;

    private String ingredients;

    private String nutritionInfo;

    private String dishImage;

    private String category;

    private Integer supplyStatus;

    private BigDecimal specialPrice;

    private LocalDateTime specialStartTime;

    private LocalDateTime specialEndTime;

    private Integer purchaseLimit;

    private Integer likeCount;

    private Integer favoriteCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
