package com.saikvt.event.repository;

import com.saikvt.event.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    //Optional<UserProfile> findByEmail(String email);
    List<UserProfile> findByEmail(String email);

    List<UserProfile> findByPhone(String phone);

    List<UserProfile> findByEmailAndPhone(String email, String phone);
}

