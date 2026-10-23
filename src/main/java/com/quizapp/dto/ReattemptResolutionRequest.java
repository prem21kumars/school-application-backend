package com.quizapp.dto;

public class ReattemptResolutionRequest {
    private String status; // approved, rejected
    private String paperMode = "same"; // same, new_random

    public ReattemptResolutionRequest() {}

    public ReattemptResolutionRequest(String status, String paperMode) {
        this.status = status;
        this.paperMode = paperMode != null ? paperMode : "same";
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaperMode() { return paperMode; }
    public void setPaperMode(String paperMode) { this.paperMode = paperMode; }
}
