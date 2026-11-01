package com.zetapilot.manager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_AUTH_PATHS = Set.of(
            "/api/auth/login", "/api/auth/refresh", "/api/auth/logout", "/api/auth/csrf");

    private final JwtTokenService jwtTokenService;
    private final ProblemDetailWriter problemDetailWriter;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, ProblemDetailWriter problemDetailWriter) {
        this.jwtTokenService = jwtTokenService;
        this.problemDetailWriter = problemDetailWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLIC_AUTH_PATHS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = resolveToken(request, authorization);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication;
        try {
            Claims claims = jwtTokenService.parse(token).getPayload();
            String subject = claims.getSubject();
            if (!StringUtils.hasText(subject)) {
                throw new JwtException("Token sem subject.");
            }

            Object rolesClaim = claims.get("roles");
            var authorities = !(rolesClaim instanceof List<?> roles) ? List.<SimpleGrantedAuthority>of() : roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .filter(role -> role.startsWith("ROLE_"))
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            authentication = new UsernamePasswordAuthenticationToken(subject, null, authorities);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            if (exception instanceof io.jsonwebtoken.ExpiredJwtException) {
                response.setHeader("X-Token-Expired", "true");
            }
            problemDetailWriter.write(request, response, HttpStatus.UNAUTHORIZED, "The token is missing, expired, or invalid.");
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request, String authorization) {
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7).trim();
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("ZETA_ACCESS".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}