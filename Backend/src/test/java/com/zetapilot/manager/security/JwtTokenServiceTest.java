package com.zetapilot.manager.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String SECRET = "manager-test-signing-secret-with-at-least-32-bytes";

    @Test
    void createsSignedAccessTokenWithSubjectAndRoles() {
        var tokenService = new JwtTokenService(SECRET, 900_000);
        String token = tokenService.createAccessToken(
                "user@example.test",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        var claims = tokenService.parse(token).getPayload();
        assertThat(claims.getSubject()).isEqualTo("user@example.test");
        assertThat((List<String>) claims.get("roles")).containsExactly("ROLE_USER");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void rejectsMissingOrWeakSecret() {
        assertThatThrownBy(() -> new JwtTokenService("", 900_000))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtTokenService("replace_with_a_real_secret_value", 900_000))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtTokenService("too-short", 900_000))
                .isInstanceOf(IllegalArgumentException.class);
    }
}