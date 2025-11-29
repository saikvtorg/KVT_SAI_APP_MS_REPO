package com.saikvt.event.service;

import com.saikvt.event.entity.Module;
import com.saikvt.event.entity.Exhibition;
import com.saikvt.event.repository.ModuleRepository;
import com.saikvt.event.repository.ExhibitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ModuleService {

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
        ex.ifPresent(module::setExhibition);
        return moduleRepository.save(module);
    }

    public Optional<Module> getModuleById(String id) {
        return moduleRepository.findById(id);
    }

    public List<Module> getModulesForExhibition(String exhibitionId) {
        return moduleRepository.findByExhibition_ExhibitionId(exhibitionId);
    }
}

