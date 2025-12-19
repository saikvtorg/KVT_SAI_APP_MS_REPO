package com.saikvt.event.controller;

import com.saikvt.event.entity.Stall;
import com.saikvt.event.service.StallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/modules/{moduleId}/stalls")
@Tag(name = "Stalls", description = "APIs to manage stalls")
public class StallController {

    private static final Logger log = LoggerFactory.getLogger(StallController.class);

    private final StallService stallService;

    public StallController(StallService stallService) {
        this.stallService = stallService;
    }

    @PostMapping
    @Operation(summary = "Create stall for module")
    public ResponseEntity<Stall> createStall(@PathVariable String moduleId, @RequestBody Stall stall) {
        Stall saved = stallService.createStall(moduleId, stall);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get stalls for module")
    public ResponseEntity<List<Stall>> getStalls(@PathVariable String moduleId) {
        try {
            List<Stall> list = stallService.getStallsForModule(moduleId);
            if (list == null || list.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            log.error("Error while fetching stalls for module {}", moduleId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/{stallId}")
    @Operation(summary = "Get stall by id")
    public ResponseEntity<Stall> getStall(@PathVariable String moduleId, @PathVariable String stallId) {
        Optional<Stall> s = stallService.getStallById(stallId);
        return s.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
