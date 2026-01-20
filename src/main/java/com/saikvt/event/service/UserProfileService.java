package com.saikvt.event.service;

import com.saikvt.event.entity.UserProfile;
import com.saikvt.event.exception.ConflictException;
import com.saikvt.event.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

    private final UserProfileRepository repo;

    private static final Pattern LETTER = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserProfileService(UserProfileRepository repo) {
        this.repo = repo;
    }

    public UserProfile create(UserProfile profile) {
        // basic validation
        if (profile == null) {
            throw new IllegalArgumentException("Profile cannot be null");
        }

        // if id is null generate one
        if (profile.getUserId() == null || profile.getUserId().isEmpty()) {
            profile.setUserId(java.util.UUID.randomUUID().toString());
        }

        // Password validation if provided
        if (profile.getPassword() != null && !profile.getPassword().isBlank()) {
            if (!isValidPassword(profile.getPassword())) {
                throw new IllegalArgumentException("Password must be at least 8 characters and include letters, numbers and a special character");
            }
            // encode password before persisting
            profile.setPassword(passwordEncoder.encode(profile.getPassword()));
        }

        // Check uniqueness of email and phone
        if (profile.getEmail() != null && !profile.getEmail().isBlank()) {
            List<UserProfile> byEmail = repo.findByEmail(profile.getEmail());
            if (byEmail != null && !byEmail.isEmpty()) {
                throw new ConflictException("Email already exists");
            }
        }
        if (profile.getPhone() != null && !profile.getPhone().isBlank()) {
            List<UserProfile> byPhone = repo.findByPhone(profile.getPhone());
            if (byPhone != null && !byPhone.isEmpty()) {
                throw new ConflictException("Phone already exists");
            }
        }

        return repo.save(profile);
    }

    public Optional<UserProfile> get(String id) {
        return repo.findById(id);
    }

    public List<UserProfile> list() {
        try {
            return repo.findAll();
        } catch (Exception ex) {
            log.error("Error fetching user profiles", ex);
            return Collections.emptyList();
        }
    }

    public UserProfile update(String id, UserProfile update) {
        UserProfile existing = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        // If email is changing, ensure new email is unique
        if (update.getEmail() != null && !update.getEmail().isBlank() &&
                !update.getEmail().equals(existing.getEmail())) {
            List<UserProfile> byEmail = repo.findByEmail(update.getEmail());
            if (byEmail != null && !byEmail.isEmpty()) {
                throw new ConflictException("Email already exists");
            }
            existing.setEmail(update.getEmail());
        }

        // If phone is changing, ensure new phone is unique
        if (update.getPhone() != null && !update.getPhone().isBlank() &&
                !update.getPhone().equals(existing.getPhone())) {
            List<UserProfile> byPhone = repo.findByPhone(update.getPhone());
            if (byPhone != null && !byPhone.isEmpty()) {
                throw new ConflictException("Phone already exists");
            }
            existing.setPhone(update.getPhone());
        }

        // Password should NOT be modified via this update endpoint.
        if (update.getPassword() != null && !update.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be changed via this endpoint. Use /api/users/reset-password to change password.");
        }

        existing.setFullName(update.getFullName());
        existing.setPreferredLanguage(update.getPreferredLanguage());
        existing.setCountry(update.getCountry());
        existing.setAddress(update.getAddress());
        existing.setGender(update.getGender());
        return repo.save(existing);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }

    public List<UserProfile> findByEmailAndOrPhone(String email, String phone) {
        if (email != null && phone != null) {
            return repo.findByEmailAndPhone(email, phone);
        }
        if (email != null) {
            return repo.findByEmail(email);
        }
        return repo.findByPhone(phone);
    }

    // Backwards-compatible alias used by some controller calls / analyzers
    public List<UserProfile> findByEmailAndPhone(String email, String phone) {
        return findByEmailAndOrPhone(email, phone);
    }

    public void resetPassword(String email, String phone, String newPassword) {
        // Require both email and phone per stricter security requirement
        if (email == null || email.isBlank() || phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Both email and phone are required to reset password");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("newPassword is required");
        }
        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Password must be at least 8 characters and include letters, numbers and a special character");
        }

        List<UserProfile> candidates;
        candidates = repo.findByEmailAndPhone(email, phone);

        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("User not found for provided email/phone");
        }
        // choose first match
        UserProfile user = candidates.get(0);
        user.setPassword(passwordEncoder.encode(newPassword));
        repo.save(user);
    }

    private boolean isValidPassword(String pw) {
        if (pw == null) return false;
        if (pw.length() < 8) return false;
        if (!LETTER.matcher(pw).matches()) return false;
        if (!DIGIT.matcher(pw).matches()) return false;
        if (!SPECIAL.matcher(pw).matches()) return false;
        return true;
    }
}
