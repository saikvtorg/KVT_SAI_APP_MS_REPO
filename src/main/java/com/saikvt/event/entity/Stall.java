package com.saikvt.event.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Stall {
    @Id
    private String stallId;
    private String name;
    private String description;
    private String stallNumber;
    private String layout;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;

    @OneToMany(mappedBy = "stall", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PosterContent> posterContent;
    // Getters and Setters
}
