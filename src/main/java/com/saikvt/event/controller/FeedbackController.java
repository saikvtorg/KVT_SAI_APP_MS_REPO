package com.saikvt.event.controller;

import com.saikvt.event.entity.Feedback;
import com.saikvt.event.service.FeedbackService;
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
@RequestMapping("/api/feedbacks")
@Tag(name = "Feedback", description = "APIs to manage feedbacks")
public class FeedbackController {

    private static final Logger log = LoggerFactory.getLogger(FeedbackController.class);

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Feedback> create(@RequestBody Feedback f) {
        Feedback created = service.create(f);
        return ResponseEntity.created(URI.create("/api/feedbacks/" + created.getFeedbackId())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feedback> get(@PathVariable String id) {
        Feedback f = service.get(id);
        if (f == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(f);
    }

    @GetMapping
    public ResponseEntity<List<Feedback>> listAll() {
        try {
            List<Feedback> list = service.listAll();
            if (list == null || list.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            log.error("Error while fetching all feedbacks", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/exhibition/{exhibitionId}")
    public ResponseEntity<List<Feedback>> listByExhibition(@PathVariable String exhibitionId) {
        try {
            List<Feedback> list = service.listByExhibition(exhibitionId);
            if (list == null || list.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            log.error("Error while fetching feedbacks for exhibition {}", exhibitionId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Feedback>> listByUser(@PathVariable String userId) {
        try {
            List<Feedback> list = service.listByUser(userId);
            if (list == null || list.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            log.error("Error while fetching feedbacks for user {}", userId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Feedback> update(@PathVariable String id, @RequestBody Feedback update) {
        try {
            Feedback updated = service.update(id, update);
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
