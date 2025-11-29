package com.saikvt.event.repository;

import com.saikvt.event.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, String> {
    List<Module> findByExhibition_ExhibitionId(String exhibitionId);
}

