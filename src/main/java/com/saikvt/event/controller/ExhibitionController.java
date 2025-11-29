package com.saikvt.event.controller;

import com.saikvt.event.entity.Exhibition;
import com.saikvt.event.service.ExhibitionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/exhibitions")
@Tag(name = "Exhibitions", description = "APIs to manage exhibitions")
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    public ExhibitionController(ExhibitionService exhibitionService) {
        this.exhibitionService = exhibitionService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exhibition by id", description = "Returns the exhibition from the database for the requested id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exhibition found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Exhibition.class))),
            @ApiResponse(responseCode = "404", description = "Exhibition not found")
    })
    public ResponseEntity<Exhibition> getExhibition(@PathVariable String id) {
        Optional<Exhibition> exOpt = exhibitionService.getExhibitionById(id);
        return exOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create exhibition", description = "Accepts an Exhibition JSON and persists it to the DB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exhibition created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Exhibition.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Exhibition payload", required = true,
            content = @Content(schema = @Schema(implementation = Exhibition.class)))
    public ResponseEntity<Exhibition> createExhibition(@RequestBody Exhibition exhibition) {
        Exhibition saved = exhibitionService.createExhibition(exhibition);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}
