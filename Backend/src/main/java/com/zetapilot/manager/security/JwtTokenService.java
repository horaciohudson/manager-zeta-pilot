package com.zetapilot.manager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {

    private final SecretKey signingKey;
    private final long accessTokenTtlMillis;

    public JwtTokenService(
            @Value("${app.jwt.secret:}") String base64Secret,
            @Value("${app.jwt.access-token-ttl:900000}") long accessTokenTtlMillis) {
        if (base64Secret == null || base64Secret.isBlank()
                || base64Secret.toLowerCase(java.util.Locale.ROOT).contains("replace")
                || base64Secret.toLowerCase(java.util.Locale.ROOT).contains("change_me")
                || base64Secret.toLowerCase(java.util.Locale.ROOT).contains("changeme")) {
            throw new IllegalStateException("JWT_SECRET must be replaced with a random secret before starting the backend.");
        }
        if (accessTokenTtlMillis <= 0) {
            throw new IllegalArgumentException("JWT_EXPIRATION_MS must be greater than zero.");
        }

        try {
            byte[] secretBytes = base64Secret.getBytes(StandardCharsets.UTF_8);
            if (secretBytes.length < 32) {
                throw new IllegalArgumentException("JWT_SECRET must contain at least 32 bytes.");
            }
            this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 256 bits of entropy.", exception);
        }
        this.accessTokenTtlMillis = accessTokenTtlMillis;
    }

    public String createAccessToken(String subject, Collection<? extends GrantedAuthority> authorities) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("The token subject cannot be blank.");
        }

        Instant issuedAt = Instant.now();
        List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).distinct().toList();
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plusMillis(accessTokenTtlMillis)))
                .signWith(signingKey)
                .compact();
    }

    public long getAccessTokenTtlMillis() {
        return accessTokenTtlMillis;
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
    }
}