package com.saikvt.event.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class User {
    @Id
    private String userId;
    private String email;
    private String password;
    private String fullName;
    private String preferredLanguage;
    // Getters and Setters
}
