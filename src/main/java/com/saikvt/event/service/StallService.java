package com.saikvt.event.service;

import com.saikvt.event.entity.Module;
import com.saikvt.event.entity.Stall;
import com.saikvt.event.repository.ModuleRepository;
import com.saikvt.event.repository.StallRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StallService {

    private final StallRepository stallRepository;
    private final ModuleRepository moduleRepository;

    public StallService(StallRepository stallRepository, ModuleRepository moduleRepository) {
        this.stallRepository = stallRepository;
        this.moduleRepository = moduleRepository;
    }

    public Stall createStall(String moduleId, Stall stall) {
        if (stall.getStallId() == null || stall.getStallId().isEmpty()) {
            stall.setStallId("stall-" + UUID.randomUUID().toString());
        }
        Optional<Module> mod = moduleRepository.findById(moduleId);
        mod.ifPresent(stall::setModule);
        return stallRepository.save(stall);
    }

    public Optional<Stall> getStallById(String id) {
        return stallRepository.findById(id);
    }

    public List<Stall> getStallsForModule(String moduleId) {
        return stallRepository.findByModule_ModuleId(moduleId);
    }
}

