package com.zetapilot.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:auth-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.jwt.secret=manager-auth-test-signing-secret-at-least-32-bytes",
        "app.settings.encryption-key=manager-auth-test-encryption-secret-at-least-32-bytes",
        "app.bootstrap.enabled=false",
        "app.security.cookies.secure=false",
        "app.cors.allowed-origins=http://localhost:3000,http://localhost:5173,http://127.0.0.1:5173",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTest {

    private static final String EMAIL = "admin@example.test";
    private static final String PASSWORD = "a-strong-test-password";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @BeforeEach
    void setUpSchemaAndUser() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tab_roles (role_id UUID PRIMARY KEY, name VARCHAR(50) NOT NULL UNIQUE)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tab_users (user_id UUID PRIMARY KEY, email VARCHAR(255) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(255) NOT NULL, full_name VARCHAR(150), active BOOLEAN NOT NULL, email_verified BOOLEAN NOT NULL)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tab_users_roles (user_id UUID NOT NULL, role_id UUID NOT NULL, PRIMARY KEY(user_id, role_id))");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tab_refresh_tokens (refresh_token_id UUID PRIMARY KEY, user_id UUID NOT NULL, " +
                "token_hash VARCHAR(255) NOT NULL UNIQUE, expires_at TIMESTAMP NOT NULL, revoked BOOLEAN NOT NULL DEFAULT FALSE, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        jdbcTemplate.update("DELETE FROM tab_refresh_tokens");
        jdbcTemplate.update("DELETE FROM tab_users_roles");
        jdbcTemplate.update("DELETE FROM tab_users");
        jdbcTemplate.update("DELETE FROM tab_roles");

        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tab_roles (role_id, name) VALUES (?, ?)", roleId, "ROLE_ADMIN");
        jdbcTemplate.update("INSERT INTO tab_users (user_id, email, password_hash, full_name, active, email_verified) " +
                "VALUES (?, ?, ?, ?, TRUE, TRUE)", userId, EMAIL, passwordEncoder.encode(PASSWORD), "Test Administrator");
        jdbcTemplate.update("INSERT INTO tab_users_roles (user_id, role_id) VALUES (?, ?)", userId, roleId);
    }

    @Test
    void loginIssuesHttpOnlyCookiesAndRefreshRotatesTheRefreshToken() throws Exception {
        HttpHeaders csrfHeaders = new HttpHeaders();
        var csrfResponse = restTemplate.exchange(url("/api/auth/csrf"), HttpMethod.GET,
                new HttpEntity<>(csrfHeaders), String.class);
        assertThat(csrfResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String csrfToken = objectMapper.readTree(csrfResponse.getBody()).path("token").asText();
        String csrfCookie = findCookie(csrfResponse.getHeaders().get(HttpHeaders.SET_COOKIE), "XSRF-TOKEN");

        HttpHeaders loginHeaders = jsonHeaders(csrfToken, csrfCookie);
        var loginResponse = restTemplate.exchange(url("/api/auth/login"), HttpMethod.POST,
                new HttpEntity<>("""
                        {"email":"ADMIN@example.test","password":"a-strong-test-password"}
                        """, loginHeaders), String.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).contains(EMAIL).doesNotContain("accessToken", "refreshToken");
        List<String> loginCookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
        String accessCookie = findCookie(loginCookies, "ZETA_ACCESS");
        String firstRefreshCookie = findCookie(loginCookies, "ZETA_REFRESH");
        assertThat(loginCookies).anySatisfy(cookie -> assertThat(cookie).contains("ZETA_ACCESS=").contains("HttpOnly"));
        assertThat(loginCookies).anySatisfy(cookie -> assertThat(cookie).contains("ZETA_REFRESH=").contains("HttpOnly"));

        HttpHeaders meHeaders = new HttpHeaders();
        meHeaders.add(HttpHeaders.COOKIE, accessCookie);
        var meResponse = restTemplate.exchange(url("/api/auth/me"), HttpMethod.GET,
                new HttpEntity<>(meHeaders), String.class);
        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(meResponse.getBody()).contains(EMAIL, "ROLE_ADMIN");

        HttpHeaders refreshHeaders = jsonHeaders(csrfToken, csrfCookie);
        refreshHeaders.add(HttpHeaders.COOKIE, firstRefreshCookie);
        var refreshResponse = restTemplate.exchange(url("/api/auth/refresh"), HttpMethod.POST,
                new HttpEntity<>(refreshHeaders), String.class);
        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String rotatedRefreshCookie = findCookie(refreshResponse.getHeaders().get(HttpHeaders.SET_COOKIE), "ZETA_REFRESH");
        assertThat(rotatedRefreshCookie).isNotEqualTo(firstRefreshCookie);
        assertThat(refreshResponse.getBody()).doesNotContain("accessToken", "refreshToken");
    }

    @Test
    void loginRejectsInvalidCredentialsWithEnglishProblemDetail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@example.test","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid email or password")));
    }

    @Test
    void viteDevelopmentOriginPassesCorsPreflightForCsrfLoginHeader() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,x-xsrf-token"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Headers",
                                org.hamcrest.Matchers.containsString("x-xsrf-token")));
    }

    private HttpHeaders jsonHeaders(String csrfToken, String csrfCookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-XSRF-TOKEN", csrfToken);
        headers.add(HttpHeaders.COOKIE, csrfCookie);
        return headers;
    }

    private String findCookie(List<String> setCookies, String name) {
        assertThat(setCookies).isNotNull();
        return setCookies.stream()
                .filter(cookie -> cookie.startsWith(name + "="))
                .map(cookie -> cookie.substring(0, cookie.indexOf(';')))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected cookie was not set: " + name));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}