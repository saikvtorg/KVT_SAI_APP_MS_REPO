package com.saikvt.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request payload")
public class LoginRequest {

    @Schema(description = "User id (UUID) - optional if email provided")
    private String userId;

    @Schema(description = "Email address - optional if userId provided")
    private String email;

    @NotBlank
    @Schema(description = "Password", required = true)
    private String password;

    public LoginRequest() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

