package com.saikvt.event.service;

import com.saikvt.event.entity.Exhibition;
import com.saikvt.event.repository.ExhibitionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ExhibitionService {

    private final ExhibitionRepository repository;

    public ExhibitionService(ExhibitionRepository repository) {
        this.repository = repository;
    }

    public Exhibition createExhibition(Exhibition exhibition) {
        if (exhibition.getExhibitionId() == null || exhibition.getExhibitionId().isEmpty()) {
            exhibition.setExhibitionId("ex-" + UUID.randomUUID().toString());
        }
        if (exhibition.getStatus() == null) {
            exhibition.setStatus("CREATED");
        }
        return repository.save(exhibition);
    }

    public Optional<Exhibition> getExhibitionById(String id) {
        return repository.findById(id);
    }
}

