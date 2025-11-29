package com.saikvt.event.repository;

import com.saikvt.event.entity.PosterContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PosterContentRepository extends JpaRepository<PosterContent, String> {
    List<PosterContent> findByStall_StallId(String stallId);
}

