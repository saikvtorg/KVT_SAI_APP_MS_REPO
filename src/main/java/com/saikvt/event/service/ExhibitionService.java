package com.saikvt.event.service;

import com.saikvt.event.entity.Exhibition;
import com.saikvt.event.repository.ExhibitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExhibitionService {

    private static final Logger log = LoggerFactory.getLogger(ExhibitionService.class);

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

    public List<Exhibition> listAll() {
        try {
            List<Exhibition> all = repository.findAll();
            return all == null ? Collections.emptyList() : all;
        } catch (Exception ex) {
            log.error("Error fetching exhibitions", ex);
            return Collections.emptyList();
        }
    }
}
