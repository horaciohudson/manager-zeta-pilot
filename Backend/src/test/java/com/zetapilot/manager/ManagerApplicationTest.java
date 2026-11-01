package com.zetapilot.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:manager-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.jwt.secret=manager-test-signing-secret-with-at-least-32-bytes",
        "app.settings.encryption-key=manager-test-settings-encryption-secret-with-at-least-32-bytes",
        "app.bootstrap.enabled=false",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
})
@ActiveProfiles("test")
class ManagerApplicationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void contextLoads() {
    }

    @Test
    void healthEndpointIsPublicAndReportsUp() {
        var response = restTemplate.getForEntity("http://localhost:" + port + "/api/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void protectedEndpointUsesProblemDetailWhenUnauthenticated() {
        var response = restTemplate.getForEntity("http://localhost:" + port + "/api/private-probe", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody()).contains("\"status\":401");
    }

    @Test
    void smtpAdministrationEndpointRequiresAuthentication() {
        var response = restTemplate.getForEntity("http://localhost:" + port + "/api/admin/settings/smtp", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void malformedBearerTokenIsRejectedWithProblemDetail() {
        var headers = new org.springframework.http.HttpHeaders();
        headers.setBearerAuth("not-a-valid-jwt");
        var request = new org.springframework.http.HttpEntity<>(headers);
        var response = restTemplate.exchange(
                "http://localhost:" + port + "/api/private-probe",
                org.springframework.http.HttpMethod.GET,
                request,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    }
}