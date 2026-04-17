package com.jcen.medpal.model.dto.food;

import lombok.Data;

@Data
public class DynamicAuditRequest {
    private String auditStatus;
    private String auditReason;
}
