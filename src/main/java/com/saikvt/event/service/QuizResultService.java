package com.saikvt.event.service;

import com.saikvt.event.dto.QuizResultSummary;
import com.saikvt.event.entity.QuizResult;
import com.saikvt.event.repository.QuizResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuizResultService {
    private static final Logger log = LoggerFactory.getLogger(QuizResultService.class);

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
    public List<QuizResult> listByUser(String userId) {
        try {
            List<QuizResult> all = repo.findByUserId(userId);
            return all == null ? Collections.emptyList() : all;
        } catch (Exception ex) {
            log.error("Error fetching quiz results for user {}", userId, ex);
            return Collections.emptyList();
        }
    }
    public List<QuizResult> listByModule(String moduleId) {
        try {
            List<QuizResult> all = repo.findByModuleId(moduleId);
            return all == null ? Collections.emptyList() : all;
        } catch (Exception ex) {
            log.error("Error fetching quiz results for module {}", moduleId, ex);
            return Collections.emptyList();
        }
    }

    public List<QuizResultSummary> summaryForAllUsers() {
        try {
            List<QuizResult> all = repo.findAll();
            if (all == null || all.isEmpty()) return Collections.emptyList();

            Map<String, List<QuizResult>> byUser = all.stream().collect(Collectors.groupingBy(QuizResult::getUserId));
            List<QuizResultSummary> summaries = new ArrayList<>();
            for (Map.Entry<String, List<QuizResult>> e : byUser.entrySet()) {
                String userId = e.getKey();
                List<QuizResult> list = e.getValue();
                int total = list.size();
                double avgPct = list.stream().filter(q -> q.getPercentage() != null).mapToDouble(QuizResult::getPercentage).average().orElse(0.0);
                int totalPoints = list.stream().filter(q -> q.getPoints() != null).mapToInt(QuizResult::getPoints).sum();
                Instant last = list.stream().filter(q -> q.getTakenAt() != null).map(QuizResult::getTakenAt).max(Comparator.naturalOrder()).orElse(null);
                summaries.add(new QuizResultSummary(userId, total, avgPct, totalPoints, last));
            }
            return summaries;
        } catch (Exception ex) {
            log.error("Error building quiz result summary for all users", ex);
            return Collections.emptyList();
        }
    }

    public Optional<QuizResultSummary> summaryForUser(String userId) {
        try {
            List<QuizResult> list = repo.findByUserId(userId);
            if (list == null || list.isEmpty()) return Optional.empty();
            int total = list.size();
            double avgPct = list.stream().filter(q -> q.getPercentage() != null).mapToDouble(QuizResult::getPercentage).average().orElse(0.0);
            int totalPoints = list.stream().filter(q -> q.getPoints() != null).mapToInt(QuizResult::getPoints).sum();
            Instant last = list.stream().filter(q -> q.getTakenAt() != null).map(QuizResult::getTakenAt).max(Comparator.naturalOrder()).orElse(null);
            return Optional.of(new QuizResultSummary(userId, total, avgPct, totalPoints, last));
        } catch (Exception ex) {
            log.error("Error building quiz result summary for user {}", userId, ex);
            return Optional.empty();
        }
    }
}
