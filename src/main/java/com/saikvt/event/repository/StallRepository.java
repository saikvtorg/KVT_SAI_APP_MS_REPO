package com.saikvt.event.repository;

import com.saikvt.event.entity.Stall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StallRepository extends JpaRepository<Stall, String> {
    List<Stall> findByModule_ModuleId(String moduleId);
}

