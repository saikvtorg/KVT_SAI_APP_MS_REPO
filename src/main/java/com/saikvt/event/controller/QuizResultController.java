package com.saikvt.event.controller;

import com.saikvt.event.entity.QuizResult;
import com.saikvt.event.service.QuizResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/quiz-results")
public class QuizResultController {

    private final QuizResultService service;

    public QuizResultController(QuizResultService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<QuizResult> create(@RequestBody QuizResult qr) {
        QuizResult created = service.create(qr);
        return ResponseEntity.created(URI.create("/api/quiz-results/" + created.getResultId())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResult> get(@PathVariable String id) {
        QuizResult r = service.get(id);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
    }

    @GetMapping("/user/{userId}")
    public List<QuizResult> listByUser(@PathVariable String userId) {
        return service.listByUser(userId);
    }

    @GetMapping("/module/{moduleId}")
    public List<QuizResult> listByModule(@PathVariable String moduleId) {
        return service.listByModule(moduleId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizResult> update(@PathVariable String id, @RequestBody QuizResult update) {
        try {
            QuizResult updated = service.update(id, update);
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

