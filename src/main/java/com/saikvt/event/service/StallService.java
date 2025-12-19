package com.saikvt.event.service;

import com.saikvt.event.entity.Module;
import com.saikvt.event.entity.Stall;
import com.saikvt.event.repository.ModuleRepository;
import com.saikvt.event.repository.StallRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StallService {

    private static final Logger log = LoggerFactory.getLogger(StallService.class);

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
        try {
            List<Stall> all = stallRepository.findByModule_ModuleId(moduleId);
            return all == null ? Collections.emptyList() : all;
        } catch (Exception ex) {
            log.error("Error fetching stalls for module {}", moduleId, ex);
            return Collections.emptyList();
        }
    }
}
