package com.saikvt.event.service;

import com.saikvt.event.entity.Feedback;
import com.saikvt.event.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class FeedbackService {
    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository repo;

    public FeedbackService(FeedbackRepository repo) {
        this.repo = repo;
    }

    public Feedback create(Feedback f) {
        if (f.getFeedbackId() == null || f.getFeedbackId().isEmpty()) {
            f.setFeedbackId(java.util.UUID.randomUUID().toString());
        }
        return repo.save(f);
    }

    public Feedback update(String id, Feedback update) {
        Feedback existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Feedback not found"));
        existing.setComments(update.getComments());
        existing.setRating(update.getRating());
        return repo.save(existing);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }

    public Feedback get(String id) {
        return repo.findById(id).orElse(null);
    }

    public List<Feedback> listByExhibition(String exhibitionId) {
        try {
            List<Feedback> all = repo.findByExhibitionId(exhibitionId);
            return all == null ? Collections.emptyList() : all;
        } catch (Exception ex) {
            log.error("Error fetching feedbacks for exhibition {}", exhibitionId, ex);
            return Collections.emptyList();
        }
    }

    public List<Feedback> listByUser(String userId) {
        try {
            List<Feedback> all = repo.findByUserId(userId);
            return all == null ? Collections.emptyList() : all;
        } catch (Exception ex) {
            log.error("Error fetching feedbacks for user {}", userId, ex);
            return Collections.emptyList();
        }
    }

    public List<Feedback> listByModule(String moduleId) {
        try {
            List<Feedback> all = repo.findByModuleId(moduleId);
            return all == null ? Collections.emptyList() : all;
        } catch (Exception ex) {
            log.error("Error fetching feedbacks for module {}", moduleId, ex);
            return Collections.emptyList();
        }
    }

    public List<Feedback> listAll() {
        try {
            List<Feedback> all = repo.findAll();
            return all == null ? Collections.emptyList() : all;
        } catch (Exception ex) {
            log.error("Error fetching all feedbacks", ex);
            return Collections.emptyList();
        }
    }
}
