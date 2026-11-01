package com.zetapilot.manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateSmtpSettingsRequest(
        Boolean enabled,
        @Size(max = 255, message = "SMTP host must not exceed 255 characters.") String host,
        @Min(value = 1, message = "SMTP port must be at least 1.")
        @Max(value = 65535, message = "SMTP port must not exceed 65535.") Integer port,
        @Size(max = 255, message = "SMTP username must not exceed 255 characters.") String username,
        @Size(max = 1024, message = "SMTP password must not exceed 1024 characters.") String password,
        Boolean auth,
        Boolean startTls,
        Boolean sslEnabled,
        @Email(message = "Sender address must be a valid email address.")
        @Size(max = 255, message = "Sender address must not exceed 255 characters.") String fromAddress,
        @Size(max = 150, message = "Sender name must not exceed 150 characters.") String fromName) {
}