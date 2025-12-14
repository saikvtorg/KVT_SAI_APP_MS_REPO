package com.saikvt.event.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_feedback")
public class Feedback {

    @Id
    @Column(name = "feedback_id", length = 36)
    private String feedbackId;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "exhibition_id", length = 255)
    private String exhibitionId;

    @Column(name = "comments", columnDefinition = "VARCHAR(2000)")
    private String comments;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "created_at")
    private Instant createdAt;

    public Feedback() {
        this.feedbackId = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    // getters and setters
    public String getFeedbackId() { return feedbackId; }
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getExhibitionId() { return exhibitionId; }
    public void setExhibitionId(String exhibitionId) { this.exhibitionId = exhibitionId; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

