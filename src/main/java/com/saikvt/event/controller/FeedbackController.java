package com.saikvt.event.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saikvt.event.dto.FeedbackRequest;
import com.saikvt.event.dto.FeedbackResponse;
import com.saikvt.event.entity.Feedback;
import com.saikvt.event.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feedbacks")
@Tag(name = "Feedback", description = "APIs to manage feedbacks")
public class FeedbackController {

    private static final Logger log = LoggerFactory.getLogger(FeedbackController.class);

    private final FeedbackService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @Operation(summary = "Create feedback", description = "Create a feedback record; questions is a JSON object/array stored as a blob")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Feedback created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedbackResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = FeedbackRequest.class),
                    examples = @ExampleObject(value = "{\n  \"userId\": \"user-uuid-123\",\n  \"exhibitionId\": \"exh-001\",\n  \"moduleId\": \"mod-001\",\n  \"comments\": \"Great exhibition, loved the interactivity.\",\n  \"rating\": 5,\n  \"questions\": {\n    \"q1\": { \"text\": \"Did you enjoy the exhibition?\", \"type\": \"boolean\", \"answer\": true },\n    \"q2\": { \"text\": \"What did you like most?\", \"type\": \"text\", \"answer\": \"The VR demo\" }\n  }\n}")
            )
    )
    @PostMapping
    public ResponseEntity<FeedbackResponse> create(@RequestBody FeedbackRequest req) {
        Feedback f = new Feedback();
        f.setUserId(req.getUserId());
        f.setExhibitionId(req.getExhibitionId());
        f.setModuleId(req.getModuleId());
        f.setComments(req.getComments());
        f.setRating(req.getRating());
        if (req.getQuestions() != null) {
            f.setQuestions(req.getQuestions().toString());
        }
        Feedback created = service.create(f);
        FeedbackResponse resp = toResponse(created);
        return ResponseEntity.created(URI.create("/api/feedbacks/" + created.getFeedbackId())).body(resp);
    }

    @Operation(summary = "Get feedback by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedback returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedbackResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> get(@PathVariable String id) {
        Feedback f = service.get(id);
        if (f == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toResponse(f));
    }

    @Operation(summary = "List all feedbacks")
    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> listAll() {
        try {
            List<Feedback> list = service.listAll();
            if (list == null || list.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<FeedbackResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            log.error("Error while fetching all feedbacks", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @Operation(summary = "List feedbacks by exhibition")
    @GetMapping("/exhibition/{exhibitionId}")
    public ResponseEntity<List<FeedbackResponse>> listByExhibition(@PathVariable String exhibitionId) {
        try {
            List<Feedback> list = service.listByExhibition(exhibitionId);
            if (list == null || list.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<FeedbackResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            log.error("Error while fetching feedbacks for exhibition {}", exhibitionId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @Operation(summary = "List feedbacks by user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedbackResponse>> listByUser(@PathVariable String userId) {
        try {
            List<Feedback> list = service.listByUser(userId);
            if (list == null || list.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<FeedbackResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            log.error("Error while fetching feedbacks for user {}", userId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @Operation(summary = "List feedbacks by module")
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<FeedbackResponse>> listByModule(@PathVariable String moduleId) {
        try {
            List<Feedback> list = service.listByModule(moduleId);
            if (list == null || list.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<FeedbackResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            log.error("Error while fetching feedbacks for module {}", moduleId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @Operation(summary = "Update feedback by id", description = "Update comments, rating, and questions (JSON) for a feedback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedback updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedbackResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = FeedbackRequest.class),
                    examples = @ExampleObject(value = "{\n  \"comments\": \"Updated comment\",\n  \"rating\": 4,\n  \"questions\": {\n    \"q1\": { \"text\": \"Did you enjoy the exhibition?\", \"type\": \"boolean\", \"answer\": false },\n    \"q4\": { \"text\": \"Any suggestions?\", \"type\": \"text\", \"answer\": \"More seating areas\" }\n  }\n}")
            )
    )
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponse> update(@PathVariable String id, @RequestBody FeedbackRequest update) {
        try {
            Feedback f = new Feedback();
            f.setComments(update.getComments());
            f.setRating(update.getRating());
            if (update.getQuestions() != null) {
                f.setQuestions(update.getQuestions().toString());
            }
            Feedback updated = service.update(id, f);
            return ResponseEntity.ok(toResponse(updated));
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private FeedbackResponse toResponse(Feedback f) {
        FeedbackResponse r = new FeedbackResponse();
        r.setFeedbackId(f.getFeedbackId());
        r.setUserId(f.getUserId());
        r.setExhibitionId(f.getExhibitionId());
        r.setModuleId(f.getModuleId());
        r.setComments(f.getComments());
        r.setRating(f.getRating());
        r.setCreatedAt(f.getCreatedAt());
        try {
            if (f.getQuestions() != null && !f.getQuestions().isBlank()) {
                JsonNode node = mapper.readTree(f.getQuestions());
                r.setQuestions(node);
            }
        } catch (Exception ex) {
            // if stored JSON is invalid, ignore and leave questions null
            log.warn("Invalid JSON stored in feedback.questions for id {}", f.getFeedbackId());
        }
        return r;
    }
}
