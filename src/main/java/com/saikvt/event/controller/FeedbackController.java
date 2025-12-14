package com.saikvt.event.controller;

import com.saikvt.event.entity.Feedback;
import com.saikvt.event.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

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

    @GetMapping("/exhibition/{exhibitionId}")
    public List<Feedback> listByExhibition(@PathVariable String exhibitionId) {
        return service.listByExhibition(exhibitionId);
    }

    @GetMapping("/user/{userId}")
    public List<Feedback> listByUser(@PathVariable String userId) {
        return service.listByUser(userId);
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

