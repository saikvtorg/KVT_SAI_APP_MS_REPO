package com.saikvt.event.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Feedback create/update request")
public class FeedbackRequest {

    private String userId;
    private String exhibitionId;
    private String moduleId;
    private String comments;
    private Integer rating;
    private JsonNode questions; // structured JSON for questions/options

    public FeedbackRequest() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExhibitionId() {
        return exhibitionId;
    }

    public void setExhibitionId(String exhibitionId) {
        this.exhibitionId = exhibitionId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public JsonNode getQuestions() {
        return questions;
    }

    public void setQuestions(JsonNode questions) {
        this.questions = questions;
    }
}

