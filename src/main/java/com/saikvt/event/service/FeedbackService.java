package com.saikvt.event.service;

import com.saikvt.event.entity.Feedback;
import com.saikvt.event.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FeedbackService {
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
        return repo.findByExhibitionId(exhibitionId);
    }

    public List<Feedback> listByUser(String userId) {
        return repo.findByUserId(userId);
    }

    public List<Feedback> listAll() {
        return repo.findAll();
    }
}
