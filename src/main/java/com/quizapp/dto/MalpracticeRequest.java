package com.quizapp.dto;

public class MalpracticeRequest {
    private String type; // tab_switch, key_press, mouse_leave, fullscreen_exit, copy_paste, devtools
    private String description;

    public MalpracticeRequest() {}

    public MalpracticeRequest(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
