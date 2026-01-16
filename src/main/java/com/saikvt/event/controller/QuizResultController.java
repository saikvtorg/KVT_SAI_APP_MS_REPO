package com.saikvt.event.controller;

import com.saikvt.event.dto.QuizResultSummary;
import com.saikvt.event.entity.QuizResult;
import com.saikvt.event.service.QuizResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz-results")
@Tag(name = "Quiz Result", description = "APIs to manage quiz results")
public class QuizResultController {

    private static final Logger log = LoggerFactory.getLogger(QuizResultController.class);

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
    public ResponseEntity<List<QuizResult>> listByUser(@PathVariable String userId) {
        try {
            List<QuizResult> list = service.listByUser(userId);
            if (list == null || list.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            log.error("Error while fetching quiz results for user {}", userId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<QuizResult>> listByModule(@PathVariable String moduleId) {
        try {
            List<QuizResult> list = service.listByModule(moduleId);
            if (list == null || list.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            log.error("Error while fetching quiz results for module {}", moduleId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
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

    @Operation(summary = "Get quiz result summary report")
    @GetMapping(value = "/report")
    public ResponseEntity<?> report(@RequestParam(required = false) String userId,
                                    @RequestParam(required = false) String format,
                                    @RequestHeader(value = "Accept", required = false) String acceptHeader) {
        boolean csv = "csv".equalsIgnoreCase(format) || (acceptHeader != null && acceptHeader.contains("text/csv"));

        if (userId != null && !userId.isBlank()) {
            Optional<QuizResultSummary> s = service.summaryForUser(userId);
            if (s.isEmpty()) return ResponseEntity.notFound().build();
            QuizResultSummary summary = s.get();
            if (csv) {
                String csvBody = toCsv(List.of(summary));
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quiz-summary-" + userId + ".csv")
                        .contentType(MediaType.valueOf("text/csv"))
                        .body(csvBody);
            }
            return ResponseEntity.ok(summary);
        } else {
            List<QuizResultSummary> all = service.summaryForAllUsers();
            if (csv) {
                String csvBody = toCsv(all);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quiz-summary-all.csv")
                        .contentType(MediaType.valueOf("text/csv"))
                        .body(csvBody);
            }
            return ResponseEntity.ok(all);
        }
    }

    private String toCsv(List<QuizResultSummary> list) {
        // header
        StringBuilder sb = new StringBuilder();
        sb.append("userId,totalQuizzes,averagePercentage,totalPoints,lastTakenAt\n");
        for (QuizResultSummary s : list) {
            sb.append(escapeCsv(s.getUserId())).append(',')
                    .append(s.getTotalQuizzes()).append(',')
                    .append(String.format("%.2f", s.getAveragePercentage())).append(',')
                    .append(s.getTotalPoints()).append(',')
                    .append(s.getLastTakenAt() == null ? "" : s.getLastTakenAt().toString())
                    .append('\n');
        }
        return sb.toString();
    }

    private String escapeCsv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\n") || v.contains("\"")) {
            return '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }
}
