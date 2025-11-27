package com.saikvt.event.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class PosterContent {
    @Id
    private String contentId;
    private String languageCode;
    private String posterMediaUrl;
    private String contentText;

    @ManyToOne
    @JoinColumn(name = "stall_id")
    private Stall stall;
    // Getters and Setters
}