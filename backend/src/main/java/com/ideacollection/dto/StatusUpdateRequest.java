package com.ideacollection.dto;

public class StatusUpdateRequest {
    private String stage;
    private String stageStatus;

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getStageStatus() { return stageStatus; }
    public void setStageStatus(String stageStatus) { this.stageStatus = stageStatus; }
}
