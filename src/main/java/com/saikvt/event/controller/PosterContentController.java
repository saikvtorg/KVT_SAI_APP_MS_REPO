package com.saikvt.event.controller;

import com.saikvt.event.entity.PosterContent;
import com.saikvt.event.service.PosterContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stalls/{stallId}/contents")
@Tag(name = "PosterContent", description = "APIs to manage poster content")
public class PosterContentController {

    private final PosterContentService posterContentService;

    public PosterContentController(PosterContentService posterContentService) {
        this.posterContentService = posterContentService;
    }

    @PostMapping
    @Operation(summary = "Create poster content for stall")
    public ResponseEntity<PosterContent> createContent(@PathVariable String stallId, @RequestBody PosterContent content) {
        PosterContent saved = posterContentService.createContent(stallId, content);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get contents for stall")
    public ResponseEntity<List<PosterContent>> getContents(@PathVariable String stallId) {
        List<PosterContent> list = posterContentService.getContentsForStall(stallId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{contentId}")
    @Operation(summary = "Get content by id")
    public ResponseEntity<PosterContent> getContent(@PathVariable String stallId, @PathVariable String contentId) {
        Optional<PosterContent> p = posterContentService.getContentById(contentId);
        return p.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

