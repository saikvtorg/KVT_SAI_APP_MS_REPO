package com.saikvt.event.controller;

import com.saikvt.event.entity.Exhibition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/exhibitions")
@Tag(name = "Exhibitions", description = "APIs to manage exhibitions (dummy implementations)")
public class ExhibitionController {

    @GetMapping("/{id}")
    @Operation(summary = "Get exhibition by id", description = "Returns a hardcoded demo exhibition for the requested id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exhibition found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Exhibition.class)))
    })
    public ResponseEntity<Exhibition> getExhibition(@PathVariable String id) {
        // Dummy hardcoded response
        Exhibition ex = new Exhibition(
                id,
                "Demo Exhibition",
                "A hardcoded demo exhibition",
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                "Demo Hall",
                "UPCOMING"
        );
        return ResponseEntity.ok(ex);
    }

    @PostMapping
    @Operation(summary = "Create exhibition", description = "Accepts an Exhibition JSON and returns a created exhibition (dummy, not persisted)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exhibition created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Exhibition.class)))
    })
    // Use the fully-qualified Swagger RequestBody annotation to avoid conflict with Spring's RequestBody
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Exhibition payload", required = true,
            content = @Content(schema = @Schema(implementation = Exhibition.class)))
    public ResponseEntity<Exhibition> createExhibition(@RequestBody Exhibition exhibition) {
        // Dummy behavior: echo back the exhibition and set an ID
        if (exhibition.getExhibitionId() == null || exhibition.getExhibitionId().isEmpty()) {
            exhibition.setExhibitionId("ex-" + System.currentTimeMillis());
        }
        // Set default status if missing
        if (exhibition.getStatus() == null) {
            exhibition.setStatus("CREATED");
        }
        return new ResponseEntity<>(exhibition, HttpStatus.CREATED);
    }
}
