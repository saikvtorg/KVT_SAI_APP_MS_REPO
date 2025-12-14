package com.saikvt.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_quiz_result")
public class QuizResult {

    @Id
    @Column(name = "result_id", length = 36)
    private String resultId;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "module_id", length = 255)
    private String moduleId;

    @Column(name = "result")
    private Integer result;

    @Column(name = "total_marks")
    private Integer totalMarks;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "points")
    private Integer points;

    @Column(name = "taken_at")
    private Instant takenAt;

    public QuizResult() {
        this.resultId = UUID.randomUUID().toString();
        this.takenAt = Instant.now();
    }

    // getters and setters
    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public Integer getResult() { return result; }
    public void setResult(Integer result) { this.result = result; }
    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public Instant getTakenAt() { return takenAt; }
    public void setTakenAt(Instant takenAt) { this.takenAt = takenAt; }
}

