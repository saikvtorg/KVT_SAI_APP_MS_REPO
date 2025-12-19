package com.saikvt.event.service;

import com.saikvt.event.entity.Module;
import com.saikvt.event.entity.Exhibition;
import com.saikvt.event.repository.ModuleRepository;
import com.saikvt.event.repository.ExhibitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ModuleService {

    private static final Logger log = LoggerFactory.getLogger(ModuleService.class);

    private final ModuleRepository moduleRepository;
    private final ExhibitionRepository exhibitionRepository;

    public ModuleService(ModuleRepository moduleRepository, ExhibitionRepository exhibitionRepository) {
        this.moduleRepository = moduleRepository;
        this.exhibitionRepository = exhibitionRepository;
    }

    public Module createModule(String exhibitionId, Module module) {
        if (module.getModuleId() == null || module.getModuleId().isEmpty()) {
            module.setModuleId("mod-" + UUID.randomUUID().toString());
        }
        Optional<Exhibition> ex = exhibitionRepository.findById(exhibitionId);
        if (ex.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exhibition not found: " + exhibitionId);
        }
        module.setExhibition(ex.get());
        return moduleRepository.save(module);
    }

    public Optional<Module> getModuleById(String id) {
        return moduleRepository.findById(id);
    }

    public List<Module> getModulesForExhibition(String exhibitionId) {
        try {
            List<Module> all = moduleRepository.findByExhibition_ExhibitionId(exhibitionId);
            return all == null ? Collections.emptyList() : all;
        } catch (Exception ex) {
            log.error("Error fetching modules for exhibition {}", exhibitionId, ex);
            return Collections.emptyList();
        }
    }

    public List<Module> listAll() {
        try {
            List<Module> all = moduleRepository.findAll();
            return all == null ? Collections.emptyList() : all;
        } catch (Exception ex) {
            log.error("Error fetching modules", ex);
            return Collections.emptyList();
        }
    }
}
