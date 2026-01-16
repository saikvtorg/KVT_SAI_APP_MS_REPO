package com.saikvt.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Summary of quiz results for a user")
public class QuizResultSummary {

    private String userId;
    private int totalQuizzes;
    private double averagePercentage;
    private int totalPoints;
    private Instant lastTakenAt;

    public QuizResultSummary() {}

    public QuizResultSummary(String userId, int totalQuizzes, double averagePercentage, int totalPoints, Instant lastTakenAt) {
        this.userId = userId;
        this.totalQuizzes = totalQuizzes;
        this.averagePercentage = averagePercentage;
        this.totalPoints = totalPoints;
        this.lastTakenAt = lastTakenAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getTotalQuizzes() {
        return totalQuizzes;
    }

    public void setTotalQuizzes(int totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }

    public double getAveragePercentage() {
        return averagePercentage;
    }

    public void setAveragePercentage(double averagePercentage) {
        this.averagePercentage = averagePercentage;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Instant getLastTakenAt() {
        return lastTakenAt;
    }

    public void setLastTakenAt(Instant lastTakenAt) {
        this.lastTakenAt = lastTakenAt;
    }
}

