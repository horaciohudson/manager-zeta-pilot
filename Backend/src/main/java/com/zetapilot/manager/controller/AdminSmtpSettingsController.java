package com.zetapilot.manager.controller;

import com.zetapilot.manager.dto.SmtpSettingsResponse;
import com.zetapilot.manager.dto.SmtpTestEmailRequest;
import com.zetapilot.manager.dto.UpdateSmtpSettingsRequest;
import com.zetapilot.manager.service.SmtpMailService;
import com.zetapilot.manager.service.SmtpSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings/smtp")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSmtpSettingsController {

    private final SmtpSettingsService smtpSettingsService;
    private final SmtpMailService smtpMailService;

    public AdminSmtpSettingsController(SmtpSettingsService smtpSettingsService, SmtpMailService smtpMailService) {
        this.smtpSettingsService = smtpSettingsService;
        this.smtpMailService = smtpMailService;
    }

    @GetMapping
    public ResponseEntity<SmtpSettingsResponse> getSettings() {
        return ResponseEntity.ok(smtpSettingsService.getSettings());
    }

    @PatchMapping
    public ResponseEntity<SmtpSettingsResponse> updateSettings(@Valid @RequestBody UpdateSmtpSettingsRequest request) {
        return ResponseEntity.ok(smtpSettingsService.update(request));
    }

    @org.springframework.web.bind.annotation.PostMapping("/test-connection")
    public ResponseEntity<Void> testConnection() {
        smtpMailService.testConnection();
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.PostMapping("/test-email")
    public ResponseEntity<Void> sendTestEmail(@Valid @RequestBody SmtpTestEmailRequest request) {
        smtpMailService.sendTestEmail(request.to());
        return ResponseEntity.noContent().build();
    }
}