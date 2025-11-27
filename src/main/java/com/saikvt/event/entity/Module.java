package com.saikvt.event.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Module {
    @Id
    private String moduleId;
    private String name;
    private String description;
    private String assignedTeamId;

    @ManyToOne
    @JoinColumn(name = "exhibition_id")
    private Exhibition exhibition;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stall> stalls;
    // Getters and Setters
}