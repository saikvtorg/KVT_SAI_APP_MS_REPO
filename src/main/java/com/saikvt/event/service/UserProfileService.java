package com.saikvt.event.service;

import com.saikvt.event.entity.UserProfile;
import com.saikvt.event.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserProfileService {

    private final UserProfileRepository repo;

    public UserProfileService(UserProfileRepository repo) {
        this.repo = repo;
    }

    public UserProfile create(UserProfile profile) {
        // if id is null generate one
        if (profile.getUserId() == null || profile.getUserId().isEmpty()) {
            profile.setUserId(java.util.UUID.randomUUID().toString());
        }
        return repo.save(profile);
    }

    public Optional<UserProfile> get(String id) {
        return repo.findById(id);
    }

    public List<UserProfile> list() {
        return repo.findAll();
    }

    public UserProfile update(String id, UserProfile update) {
        UserProfile existing = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        existing.setFullName(update.getFullName());
        existing.setEmail(update.getEmail());
        existing.setPhone(update.getPhone());
        existing.setPreferredLanguage(update.getPreferredLanguage());
        return repo.save(existing);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }
}

