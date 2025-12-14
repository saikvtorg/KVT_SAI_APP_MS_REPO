package com.saikvt.event.controller;

import com.saikvt.event.entity.UserProfile;
import com.saikvt.event.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserProfile> create(@RequestBody UserProfile profile) {
        UserProfile created = service.create(profile);
        return ResponseEntity.created(URI.create("/api/users/" + created.getUserId())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> get(@PathVariable String id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<UserProfile> list() { return service.list(); }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> update(@PathVariable String id, @RequestBody UserProfile update) {
        try {
            UserProfile updated = service.update(id, update);
            return ResponseEntity.ok(updated);
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

