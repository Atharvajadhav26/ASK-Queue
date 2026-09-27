package com.smartqueue.config;

import com.smartqueue.entity.Role;
import com.smartqueue.entity.User;
import com.smartqueue.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds the default admin account on application startup.
 * Only creates the admin if it doesn't already exist.
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public CommandLineRunner seedDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@smartqueue.com";

            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User();
                admin.setName("System Admin");
                admin.setEmail(adminEmail);
                admin.setMobile("9999999999");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole(Role.ADMIN);
                admin.setPreferredLanguage("en");
                admin.setNotificationEmail(true);
                admin.setNotificationSms(true);
                admin.setNotificationPush(true);
                userRepository.save(admin);

                log.info("═══════════════════════════════════════════════");
                log.info("  DEFAULT ADMIN ACCOUNT CREATED");
                log.info("  Email:    admin@smartqueue.com");
                log.info("  Password: Admin@123");
                log.info("  CHANGE THIS PASSWORD IN PRODUCTION!");
                log.info("═══════════════════════════════════════════════");
            } else {
                log.info("Default admin account already exists");
            }
        };
    }
}
