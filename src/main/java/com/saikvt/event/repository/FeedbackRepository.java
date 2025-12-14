package com.saikvt.event.repository;

import com.saikvt.event.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    List<Feedback> findByExhibitionId(String exhibitionId);
    List<Feedback> findByUserId(String userId);
}

