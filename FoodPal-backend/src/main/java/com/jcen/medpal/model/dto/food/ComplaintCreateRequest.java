package com.jcen.medpal.model.dto.food;

import lombok.Data;

@Data
public class ComplaintCreateRequest {

    private Long merchantId;

    private String complaintTitle;

    private String complaintContent;

    private String evidenceUrls;
}
