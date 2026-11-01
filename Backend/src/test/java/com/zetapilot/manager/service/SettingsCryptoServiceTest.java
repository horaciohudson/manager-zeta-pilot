package com.zetapilot.manager.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettingsCryptoServiceTest {

    private static final String SECRET = "manager-test-settings-encryption-secret-with-at-least-32-bytes";

    @Test
    void encryptsAndDecryptsSecretWithoutStoringPlaintext() {
        SettingsCryptoService crypto = new SettingsCryptoService(SECRET);
        String encrypted = crypto.encrypt("smtp-password");

        assertThat(encrypted).isNotEqualTo("smtp-password");
        assertThat(crypto.decrypt(encrypted)).isEqualTo("smtp-password");
        assertThat(crypto.encrypt("same-value")).isNotEqualTo(crypto.encrypt("same-value"));
    }

    @Test
    void rejectsMissingOrShortEncryptionKey() {
        assertThatThrownBy(() -> new SettingsCryptoService(""))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new SettingsCryptoService("too-short"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsTamperedCiphertext() {
        SettingsCryptoService crypto = new SettingsCryptoService(SECRET);
        String encrypted = crypto.encrypt("smtp-password");
        byte[] payload = java.util.Base64.getDecoder().decode(encrypted);
        payload[payload.length - 1] ^= 1;

        assertThatThrownBy(() -> crypto.decrypt(java.util.Base64.getEncoder().encodeToString(payload)))
                .isInstanceOf(IllegalStateException.class);
    }
}