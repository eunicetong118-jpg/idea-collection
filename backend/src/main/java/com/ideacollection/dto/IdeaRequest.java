package com.ideacollection.dto;

import jakarta.validation.constraints.NotBlank;

public class IdeaRequest {
    @NotBlank
    private String subTopicId;

    @NotBlank
    private String title;

    private String description;

    public String getSubTopicId() { return subTopicId; }
    public void setSubTopicId(String subTopicId) { this.subTopicId = subTopicId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
