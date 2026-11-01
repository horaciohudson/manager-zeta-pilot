package com.zetapilot.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataInitializerTest {

    private JdbcTemplate jdbcTemplate;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void createsFirstAdministratorWithEncodedPasswordAndAdminRole() {
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tab_users", Integer.class)).thenReturn(0);
        DataInitializer initializer = new DataInitializer(
                jdbcTemplate, passwordEncoder, "admin@zetapilot.ai", "strong-password-123");

        initializer.run(new DefaultApplicationArguments());

        var userInsert = org.mockito.ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).update(
                eq("INSERT INTO tab_users (user_id, email, password_hash, full_name, active, email_verified) " +
                        "VALUES (?, ?, ?, ?, TRUE, TRUE)"),
                userInsert.capture());
        Object[] userValues = userInsert.getValue();
        assertThat(userValues[1]).isEqualTo("admin@zetapilot.ai");
        assertThat(passwordEncoder.matches("strong-password-123", (String) userValues[2])).isTrue();
        assertThat(userValues[2]).isNotEqualTo("strong-password-123");

        verify(jdbcTemplate).update(
                "INSERT INTO tab_users_roles (user_id, role_id) " +
                        "SELECT ?, role_id FROM tab_roles WHERE name = 'ROLE_ADMIN'",
                userValues[0]);
    }

    @Test
    void doesNotModifyUsersWhenDatabaseAlreadyHasAccounts() {
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tab_users", Integer.class)).thenReturn(1);
        DataInitializer initializer = new DataInitializer(jdbcTemplate, passwordEncoder, "", "");

        initializer.run(new DefaultApplicationArguments());

        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void refusesToBootstrapWithoutConfiguredCredentials() {
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tab_users", Integer.class)).thenReturn(0);
        DataInitializer initializer = new DataInitializer(jdbcTemplate, passwordEncoder, "", "");

        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_EMAIL and ADMIN_PASSWORD");

        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }
}