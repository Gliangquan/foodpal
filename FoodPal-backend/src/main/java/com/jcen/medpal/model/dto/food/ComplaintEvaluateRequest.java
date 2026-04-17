package com.jcen.medpal.model.dto.food;

import lombok.Data;

@Data
public class ComplaintEvaluateRequest {

    private Long complaintId;

    /**
     * 1=不满意 2=一般 3=满意
     */
    private Integer resultRating;

    private String feedback;
}
