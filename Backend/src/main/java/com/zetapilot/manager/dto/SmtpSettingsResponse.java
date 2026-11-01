package com.zetapilot.manager.dto;

public record SmtpSettingsResponse(
        boolean enabled,
        String host,
        int port,
        String username,
        boolean hasPassword,
        boolean auth,
        boolean startTls,
        boolean sslEnabled,
        String fromAddress,
        String fromName) {
}