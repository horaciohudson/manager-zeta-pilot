package com.zetapilot.manager.controller;

import com.zetapilot.manager.dto.AuthenticatedUserResponse;
import com.zetapilot.manager.dto.LoginRequest;
import com.zetapilot.manager.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String ACCESS_COOKIE = "ZETA_ACCESS";
    private static final String REFRESH_COOKIE = "ZETA_REFRESH";

    private final AuthenticationService authenticationService;
    private final long accessTokenTtlMillis;
    private final boolean secureCookies;

    public AuthController(
            AuthenticationService authenticationService,
            @Value("${app.jwt.access-token-ttl:900000}") long accessTokenTtlMillis,
            @Value("${app.security.cookies.secure:false}") boolean secureCookies) {
        this.authenticationService = authenticationService;
        this.accessTokenTtlMillis = accessTokenTtlMillis;
        this.secureCookies = secureCookies;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticatedUserResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthenticationService.SessionTokens session = authenticationService.login(request.email(), request.password());
        writeSessionCookies(response, session);
        return ResponseEntity.ok(session.user());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticatedUserResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        AuthenticationService.SessionTokens session = authenticationService.refresh(readCookie(request, REFRESH_COOKIE));
        writeSessionCookies(response, session);
        return ResponseEntity.ok(session.user());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logout(readCookie(request, REFRESH_COOKIE));
        clearCookie(response, ACCESS_COOKIE);
        clearCookie(response, REFRESH_COOKIE);
        clearCookie(response, "XSRF-TOKEN");
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse currentUser(Authentication authentication) {
        return authenticationService.currentUser(authentication.getName());
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(org.springframework.security.web.csrf.CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken());
    }

    private void writeSessionCookies(HttpServletResponse response, AuthenticationService.SessionTokens session) {
        addCookie(response, ACCESS_COOKIE, session.accessToken(), Duration.ofMillis(accessTokenTtlMillis));
        addCookie(response, REFRESH_COOKIE, session.refreshToken(), Duration.ofMillis(authenticationService.getRefreshTokenTtlMillis()));
    }

    private void addCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(!"XSRF-TOKEN".equals(name))
                .secure(secureCookies)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}