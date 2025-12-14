package com.saikvt.event.repository;

import com.saikvt.event.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, String> {
    List<QuizResult> findByUserId(String userId);
    List<QuizResult> findByModuleId(String moduleId);
}

