package com.zetapilot.manager.service;

import com.zetapilot.manager.dto.AuthenticatedUserResponse;
import com.zetapilot.manager.exception.AuthenticationFailureException;
import com.zetapilot.manager.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class AuthenticationService {

    private static final String INVALID_CREDENTIALS = "Invalid email or password.";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long refreshTokenTtlMillis;

    public AuthenticationService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            @Value("${app.auth.refresh-token-ttl:604800000}") long refreshTokenTtlMillis) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        if (refreshTokenTtlMillis <= 0) {
            throw new IllegalArgumentException("JWT_REFRESH_EXPIRATION_MS must be greater than zero.");
        }
        this.refreshTokenTtlMillis = refreshTokenTtlMillis;
    }

    @Transactional
    public SessionTokens login(String email, String password) {
        List<UserRecord> matches = jdbcTemplate.query(
                "SELECT user_id, email, password_hash, full_name, active, email_verified " +
                        "FROM tab_users WHERE lower(email) = ?",
                (rs, row) -> new UserRecord(
                        rs.getObject("user_id", UUID.class), rs.getString("email"),
                        rs.getString("password_hash"), rs.getString("full_name"),
                        rs.getBoolean("active"), rs.getBoolean("email_verified")),
                normalizeEmail(email));

        if (matches.size() != 1) {
            throw new AuthenticationFailureException(INVALID_CREDENTIALS);
        }
        UserRecord user = matches.get(0);
        if (!user.active() || !user.emailVerified() || !passwordEncoder.matches(password, user.passwordHash())) {
            throw new AuthenticationFailureException(INVALID_CREDENTIALS);
        }
        return issueSession(user, loadRoles(user.id()));
    }

    @Transactional
    public SessionTokens refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new AuthenticationFailureException("Your session has expired. Please sign in again.");
        }
        String tokenHash = hashToken(rawRefreshToken);
        List<UserRecord> matches = jdbcTemplate.query(
                "SELECT u.user_id, u.email, u.password_hash, u.full_name, u.active, u.email_verified " +
                        "FROM tab_refresh_tokens rt JOIN tab_users u ON u.user_id = rt.user_id " +
                        "WHERE rt.token_hash = ? AND rt.revoked = FALSE AND rt.expires_at > CURRENT_TIMESTAMP " +
                        "FOR UPDATE",
                (rs, row) -> new UserRecord(
                        rs.getObject("user_id", UUID.class), rs.getString("email"),
                        rs.getString("password_hash"), rs.getString("full_name"),
                        rs.getBoolean("active"), rs.getBoolean("email_verified")),
                tokenHash);
        if (matches.size() != 1) {
            throw new AuthenticationFailureException("Your session has expired. Please sign in again.");
        }

        UserRecord user = matches.get(0);
        jdbcTemplate.update("UPDATE tab_refresh_tokens SET revoked = TRUE WHERE token_hash = ?", tokenHash);
        if (!user.active() || !user.emailVerified()) {
            throw new AuthenticationFailureException("Your account is not available. Please contact an administrator.");
        }
        return issueSession(user, loadRoles(user.id()));
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            jdbcTemplate.update("UPDATE tab_refresh_tokens SET revoked = TRUE WHERE token_hash = ? AND revoked = FALSE",
                    hashToken(rawRefreshToken));
        }
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse currentUser(String email) {
        List<AuthenticatedUserResponse> users = jdbcTemplate.query(
                "SELECT user_id, email, full_name FROM tab_users WHERE lower(email) = ? AND active = TRUE",
                (rs, row) -> new AuthenticatedUserResponse(
                        rs.getObject("user_id", UUID.class), rs.getString("email"),
                        rs.getString("full_name"), loadRoles(rs.getObject("user_id", UUID.class))),
                normalizeEmail(email));
        if (users.size() != 1) {
            throw new AuthenticationFailureException(INVALID_CREDENTIALS);
        }
        return users.get(0);
    }

    private SessionTokens issueSession(UserRecord user, List<String> roleNames) {
        List<SimpleGrantedAuthority> authorities = roleNames.stream().map(SimpleGrantedAuthority::new).toList();
        String accessToken = jwtTokenService.createAccessToken(user.email(), authorities);
        byte[] randomToken = new byte[64];
        secureRandom.nextBytes(randomToken);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomToken);
        Instant expiresAt = Instant.now().plusMillis(refreshTokenTtlMillis);
        jdbcTemplate.update(
                "INSERT INTO tab_refresh_tokens (refresh_token_id, user_id, token_hash, expires_at) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), user.id(), hashToken(refreshToken), Timestamp.from(expiresAt));
        return new SessionTokens(accessToken, refreshToken, expiresAt,
                new AuthenticatedUserResponse(user.id(), user.email(), user.fullName(), roleNames));
    }

    private List<String> loadRoles(UUID userId) {
        return jdbcTemplate.queryForList(
                "SELECT r.name FROM tab_roles r JOIN tab_users_roles ur ON ur.role_id = r.role_id " +
                        "WHERE ur.user_id = ? ORDER BY r.name",
                String.class, userId);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not securely process the session token.", exception);
        }
    }

    public long getRefreshTokenTtlMillis() {
        return refreshTokenTtlMillis;
    }

    public record SessionTokens(
            String accessToken,
            String refreshToken,
            Instant refreshExpiresAt,
            AuthenticatedUserResponse user) {
    }

    private record UserRecord(
            UUID id,
            String email,
            String passwordHash,
            String fullName,
            boolean active,
            boolean emailVerified) {
    }
}