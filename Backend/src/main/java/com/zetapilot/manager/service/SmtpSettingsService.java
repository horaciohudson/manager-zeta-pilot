package com.zetapilot.manager.service;

import com.zetapilot.manager.dto.SmtpSettingsResponse;
import com.zetapilot.manager.dto.UpdateSmtpSettingsRequest;
import com.zetapilot.manager.model.SmtpSettings;
import com.zetapilot.manager.repository.SmtpSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SmtpSettingsService {

    private final SmtpSettingsRepository repository;
    private final SettingsCryptoService cryptoService;

    public SmtpSettingsService(SmtpSettingsRepository repository, SettingsCryptoService cryptoService) {
        this.repository = repository;
        this.cryptoService = cryptoService;
    }

    @Transactional(readOnly = true)
    public SmtpSettingsResponse getSettings() {
        return toResponse(repository.findById(SmtpSettings.DEFAULT_ID).orElseGet(SmtpSettings::defaults));
    }

    @Transactional
    public SmtpSettingsResponse update(UpdateSmtpSettingsRequest request) {
        SmtpSettings settings = repository.findById(SmtpSettings.DEFAULT_ID).orElseGet(SmtpSettings::defaults);
        if (request.enabled() != null) settings.setEnabled(request.enabled());
        if (request.host() != null) settings.setHost(request.host().trim());
        if (request.port() != null) settings.setPort(request.port());
        if (request.username() != null) settings.setUsername(request.username().trim());
        if (StringUtils.hasText(request.password())) settings.setPasswordEncrypted(cryptoService.encrypt(request.password()));
        if (request.auth() != null) settings.setAuth(request.auth());
        if (request.startTls() != null) settings.setStartTlsEnabled(request.startTls());
        if (request.sslEnabled() != null) settings.setSslEnabled(request.sslEnabled());
        if (request.fromAddress() != null) settings.setFromAddress(request.fromAddress().trim());
        if (request.fromName() != null) settings.setFromName(request.fromName().trim());
        return toResponse(repository.save(settings));
    }

    private SmtpSettingsResponse toResponse(SmtpSettings settings) {
        return new SmtpSettingsResponse(settings.isEnabled(), settings.getHost(), settings.getPort(),
                settings.getUsername(), StringUtils.hasText(settings.getPasswordEncrypted()), settings.isAuth(),
                settings.isStartTlsEnabled(), settings.isSslEnabled(), settings.getFromAddress(), settings.getFromName());
    }
}