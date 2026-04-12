package com.jcen.medpal.model.dto.food;

import lombok.Data;

@Data
public class ViolationRemindRequest {

    private Long merchantId;

    private String violationType;

    private String violationReason;

    private String penaltyMeasures;

    private String violationLevel;

    private String processStatus;

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public String getViolationReason() {
        return violationReason;
    }

    public void setViolationReason(String violationReason) {
        this.violationReason = violationReason;
    }

    public String getPenaltyMeasures() {
        return penaltyMeasures;
    }

    public void setPenaltyMeasures(String penaltyMeasures) {
        this.penaltyMeasures = penaltyMeasures;
    }

    public String getViolationLevel() {
        return violationLevel;
    }

    public void setViolationLevel(String violationLevel) {
        this.violationLevel = violationLevel;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
    }
}