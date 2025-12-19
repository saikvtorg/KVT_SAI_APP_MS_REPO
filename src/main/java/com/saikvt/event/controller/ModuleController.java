package com.saikvt.event.controller;

import com.saikvt.event.entity.Module;
import com.saikvt.event.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/exhibitions/{exhibitionId}/modules")
@Tag(name = "Module", description = "APIs to manage modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @PostMapping
    @Operation(summary = "Create module for exhibition")
    public ResponseEntity<Module> createModule(@PathVariable String exhibitionId, @RequestBody Module module) {
        Module saved = moduleService.createModule(exhibitionId, module);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get modules for exhibition")
    public ResponseEntity<List<Module>> getModules(@PathVariable String exhibitionId) {
        List<Module> list = moduleService.getModulesForExhibition(exhibitionId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{moduleId}")
    @Operation(summary = "Get module by id")
    public ResponseEntity<Module> getModule(@PathVariable String exhibitionId, @PathVariable String moduleId) {
        Optional<Module> m = moduleService.getModuleById(moduleId);
        return m.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // New: Global endpoint to list all modules (absolute path overrides controller-level mapping)
    @GetMapping(path = "/api/modules")
    @Operation(summary = "Get all modules", description = "Returns list of all modules across exhibitions")
    public ResponseEntity<List<Module>> listAllModulesGlobal() {
        return ResponseEntity.ok(moduleService.listAll());
    }
}
