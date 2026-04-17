package com.jcen.medpal.model.dto.food;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DishSpecialRequest {
    private Long dishId;
    private BigDecimal specialPrice;
    private LocalDateTime specialStartTime;
    private LocalDateTime specialEndTime;
    private Integer purchaseLimit;
}
