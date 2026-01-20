package com.saikvt.event.controller;

import com.saikvt.event.dto.ErrorResponse;
import com.saikvt.event.entity.UserProfile;
import com.saikvt.event.exception.ConflictException;
import com.saikvt.event.service.UserProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "APIs to manage user profiles")
public class UserProfileController {

    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);

    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserProfile profile) {
        try {
            if (profile == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Request body is required"));
            }
            UserProfile created = service.create(profile);
            return ResponseEntity.created(URI.create("/api/users/" + created.getUserId())).body(created);
        } catch (ConflictException ex) {
            String m = ex.getMessage() == null ? "Conflict" : ex.getMessage();
            String lower = m.toLowerCase();
            if (lower.contains("email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Duplicate", "Email already exists"));
            } else if (lower.contains("phone")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Duplicate", "Phone already exists"));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Conflict", m));
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid input", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error creating user profile", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> get(@PathVariable String id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserProfile>> list() {
        try {
            List<UserProfile> list = service.list();
            if (list == null || list.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            log.error("Error while fetching user profiles", ex);
            // Return empty list with 200 to keep clients resilient; also log the error for diagnostics.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfile>> getByEmailAndOrPhone(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {

        if ((email == null || email.isBlank()) &&
                (phone == null || phone.isBlank())) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        try {
            List<UserProfile> users = service.findByEmailAndPhone(email, phone);
            return ResponseEntity.ok(
                    users != null ? users : Collections.emptyList()
            );
        } catch (Exception ex) {
            log.error("Error while fetching user by email/phone", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody com.saikvt.event.dto.UserProfileUpdateRequest req) {
        try {
            if (req == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid input", "Request body is required"));
            }
            // Map DTO to entity for update; do not allow password via this DTO
            UserProfile upd = new UserProfile();
            upd.setFullName(req.getFullName());
            upd.setEmail(req.getEmail());
            upd.setPhone(req.getPhone());
            upd.setPreferredLanguage(req.getPreferredLanguage());
            upd.setCountry(req.getCountry());
            upd.setAddress(req.getAddress());
            upd.setGender(req.getGender());
            // role is intentionally not settable by normal users here; service.update will persist allowed fields only

            UserProfile updated = service.update(id, upd);
            return ResponseEntity.ok(updated);
        } catch (ConflictException ex) {
            String m = ex.getMessage() == null ? "Conflict" : ex.getMessage();
            String lower = m.toLowerCase();
            if (lower.contains("email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Duplicate", "Email already exists"));
            } else if (lower.contains("phone")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Duplicate", "Phone already exists"));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Conflict", m));
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid input", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Not found", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error updating user profile", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error", ex.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody com.saikvt.event.dto.ResetPasswordRequest req) {
        try {
            // Require both email and phone for extra safety (per your requirement)
            if (req == null || req.getEmail() == null || req.getEmail().isBlank() || req.getPhone() == null || req.getPhone().isBlank()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid input", "email and phone are required"));
            }
            service.resetPassword(req.getEmail(), req.getPhone(), req.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid input", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Not found", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error resetting password", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
