package com.jcen.medpal.model.dto.food;

import lombok.Data;

@Data
public class ComplaintProcessRequest {

    private Long complaintId;

    private String status;

    private String processProgress;

    private String rectifyRequirement;

    private String rectifyResult;

    private String feedback;

    private Integer resultRating;
}
