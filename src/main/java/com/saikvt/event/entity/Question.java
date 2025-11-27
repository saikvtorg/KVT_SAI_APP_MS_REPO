package com.saikvt.event.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Question {
    @Id
    private String questionId;
    private String questionText;
    private String answerType;
    @ElementCollection
    private List<String> options;

    @ManyToOne
    @JoinColumn(name = "questionnaire_id")
    private Questionnaire questionnaire;
    // Getters and Setters
}
