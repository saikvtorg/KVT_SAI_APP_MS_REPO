package com.saikvt.event.controller;

import com.saikvt.event.dto.LoginRequest;
import com.saikvt.event.entity.UserProfile;
import com.saikvt.event.repository.UserProfileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final UserProfileRepository userRepo;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserProfileRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Operation(summary = "Authenticate a user", description = "Authenticate a user by userId or email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - missing credentials"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest login) {
        String userId = login.getUserId();
        String email = login.getEmail();
        String password = login.getPassword();

        if ((userId == null || userId.isBlank()) && (email == null || email.isBlank())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "userId or email is required"));
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "password is required"));
        }

        Optional<UserProfile> opt = Optional.empty();
        if (userId != null && !userId.isBlank()) {
            opt = userRepo.findById(userId);
        } else if (email != null && !email.isBlank()) {
            List<UserProfile> list = userRepo.findByEmail(email);
            if (list != null && !list.isEmpty()) {
                opt = Optional.of(list.get(0));
            }
        }

        if (opt.isEmpty()) {
            // user not found
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        UserProfile user = opt.get();
        String stored = user.getPassword();
        if (stored == null || stored.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        boolean matches;
        // detect bcrypt hash by $2a$, $2b$, $2y$ prefix
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            matches = passwordEncoder.matches(password, stored);
        } else {
            // legacy plaintext comparison
            matches = stored.equals(password);
            if (matches) {
                // re-hash and persist the password so future checks are hashed
                user.setPassword(passwordEncoder.encode(password));
                userRepo.save(user);
            }
        }

        if (!matches) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        // Login successful - return minimal response (no JWT implemented)
        return ResponseEntity.ok(Map.of(
                "userId", user.getUserId(),
                "success", true,
                "role", user.getRole()
        ));
    }
}
