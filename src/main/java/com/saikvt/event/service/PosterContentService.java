package com.saikvt.event.service;

import com.saikvt.event.entity.PosterContent;
import com.saikvt.event.entity.Stall;
import com.saikvt.event.repository.PosterContentRepository;
import com.saikvt.event.repository.StallRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PosterContentService {

    private static final Logger log = LoggerFactory.getLogger(PosterContentService.class);

    private final PosterContentRepository posterContentRepository;
    private final StallRepository stallRepository;

    public PosterContentService(PosterContentRepository posterContentRepository, StallRepository stallRepository) {
        this.posterContentRepository = posterContentRepository;
        this.stallRepository = stallRepository;
    }

    public PosterContent createContent(String stallId, PosterContent content) {
        if (content.getContentId() == null || content.getContentId().isEmpty()) {
            content.setContentId("pc-" + UUID.randomUUID().toString());
        }
        Optional<Stall> stall = stallRepository.findById(stallId);
        stall.ifPresent(content::setStall);
        return posterContentRepository.save(content);
    }

    public Optional<PosterContent> getContentById(String id) {
        return posterContentRepository.findById(id);
    }

    public List<PosterContent> getContentsForStall(String stallId) {
        try {
            List<PosterContent> all = posterContentRepository.findByStall_StallId(stallId);
            return all == null ? Collections.emptyList() : all;
        } catch (Exception ex) {
            log.error("Error fetching poster contents for stall {}", stallId, ex);
            return Collections.emptyList();
        }
    }
}
