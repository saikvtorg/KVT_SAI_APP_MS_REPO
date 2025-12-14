package com.saikvt.event.service;

import com.saikvt.event.entity.QuizResult;
import com.saikvt.event.repository.QuizResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class QuizResultService {
    private final QuizResultRepository repo;

    public QuizResultService(QuizResultRepository repo) {
        this.repo = repo;
    }

    public QuizResult create(QuizResult qr) {
        if (qr.getResultId() == null || qr.getResultId().isEmpty()) {
            qr.setResultId(java.util.UUID.randomUUID().toString());
        }
        // compute percentage if not set
        if (qr.getTotalMarks() != null && qr.getResult() != null) {
            qr.setPercentage( ( qr.getResult().doubleValue() / qr.getTotalMarks().doubleValue() ) * 100.0 );
        }
        return repo.save(qr);
    }

    public QuizResult update(String id, QuizResult update) {
        QuizResult existing = repo.findById(id).orElseThrow(() -> new RuntimeException("QuizResult not found"));
        existing.setResult(update.getResult());
        existing.setTotalMarks(update.getTotalMarks());
        if (existing.getTotalMarks() != null && existing.getResult() != null) {
            existing.setPercentage((existing.getResult().doubleValue() / existing.getTotalMarks().doubleValue()) * 100.0);
        }
        existing.setPoints(update.getPoints());
        return repo.save(existing);
    }

    public void delete(String id) { repo.deleteById(id); }
    public QuizResult get(String id) { return repo.findById(id).orElse(null); }
    public List<QuizResult> listByUser(String userId) { return repo.findByUserId(userId); }
    public List<QuizResult> listByModule(String moduleId) { return repo.findByModuleId(moduleId); }
}

