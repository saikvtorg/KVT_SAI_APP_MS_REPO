package com.saikvt.event.controller;

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
    public ResponseEntity<UserProfile> create(@RequestBody UserProfile profile) {
        try {
            if (profile == null) {
                return ResponseEntity.badRequest().build();
            }
            UserProfile created = service.create(profile);
            return ResponseEntity.created(URI.create("/api/users/" + created.getUserId())).body(created);
        } catch (ConflictException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
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
    public ResponseEntity<UserProfile> update(@PathVariable String id, @RequestBody UserProfile update) {
        try {
            UserProfile updated = service.update(id, update);
            return ResponseEntity.ok(updated);
        } catch (ConflictException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
