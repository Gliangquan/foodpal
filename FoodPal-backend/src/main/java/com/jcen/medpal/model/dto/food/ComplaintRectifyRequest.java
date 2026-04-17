package com.jcen.medpal.model.dto.food;

import lombok.Data;

@Data
public class ComplaintRectifyRequest {

    private Long complaintId;

    private String rectifyResult;

    private String evidenceUrls;
}
