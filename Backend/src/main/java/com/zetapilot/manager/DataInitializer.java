package com.zetapilot.manager;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin-email:}") String adminEmail,
            @Value("${app.bootstrap.admin-password:}") String adminPassword) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tab_users", Integer.class);
        if (userCount != null && userCount > 0) {
            return;
        }

        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException(
                    "Set ADMIN_EMAIL and ADMIN_PASSWORD to bootstrap the first administrator.");
        }
        if (adminPassword.length() < 12) {
            throw new IllegalStateException("ADMIN_PASSWORD must contain at least 12 characters.");
        }

        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO tab_users (user_id, email, password_hash, full_name, active, email_verified) " +
                        "VALUES (?, ?, ?, ?, TRUE, TRUE)",
                userId, adminEmail.trim().toLowerCase(java.util.Locale.ROOT),
                passwordEncoder.encode(adminPassword), "ZetaPilotAI Administrator");
        jdbcTemplate.update(
                "INSERT INTO tab_users_roles (user_id, role_id) " +
                        "SELECT ?, role_id FROM tab_roles WHERE name = 'ROLE_ADMIN'",
                userId);
    }
}