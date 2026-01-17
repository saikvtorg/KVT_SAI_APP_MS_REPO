package com.saikvt.event.config;

import com.saikvt.event.entity.UserProfile;
import com.saikvt.event.service.UserProfileService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final UserProfileService userProfileService;

    public AdminInitializer(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Check if admin user exists by email
        List<UserProfile> existing = userProfileService.findByEmailAndPhone("saikvtorg@gmail.com", null);
        if (existing == null || existing.isEmpty()) {
            UserProfile admin = new UserProfile();
            admin.setFullName("saikvtadm");
            admin.setEmail("saikvtorg@gmail.com");
            admin.setPhone("9663566661");
            admin.setGender("M");
            admin.setCountry("India");
            admin.setAddress("India");
            admin.setPassword("saikvt@123");
            admin.setRole("admin");
            userProfileService.create(admin);
        }
    }
}

