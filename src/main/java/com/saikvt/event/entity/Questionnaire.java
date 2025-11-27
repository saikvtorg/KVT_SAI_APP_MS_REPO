package com.saikvt.event.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Questionnaire {
    @Id
    private String questionnaireId;
    private String name;
    private String description;
    private String status;

    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
    // Getters and Setters
}