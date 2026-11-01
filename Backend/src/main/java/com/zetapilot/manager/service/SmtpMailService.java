package com.zetapilot.manager.service;

import com.zetapilot.manager.model.SmtpSettings;
import com.zetapilot.manager.repository.SmtpSettingsRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Service
public class SmtpMailService {

    private final SmtpSettingsRepository repository;
    private final SettingsCryptoService cryptoService;

    public SmtpMailService(SmtpSettingsRepository repository, SettingsCryptoService cryptoService) {
        this.repository = repository;
        this.cryptoService = cryptoService;
    }

    public void testConnection() {
        try {
            buildSender(requireEnabledSettings()).testConnection();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not connect to the SMTP server. Check the settings.", exception);
        }
    }

    public void sendTestEmail(String recipient) {
        SmtpSettings settings = requireEnabledSettings();
        try {
            JavaMailSenderImpl sender = buildSender(settings);
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(recipient);
            helper.setSubject("SMTP configuration test — ZetaPilotAI Manager");
            helper.setText("<p>ZetaPilotAI Manager email delivery is working.</p>", true);
            if (StringUtils.hasText(settings.getFromName())) {
                helper.setFrom(settings.getFromAddress(), settings.getFromName());
            } else {
                helper.setFrom(settings.getFromAddress());
            }
            sender.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not send the test email. Check the SMTP configuration.", exception);
        }
    }

    private SmtpSettings requireEnabledSettings() {
        SmtpSettings settings = repository.findById(SmtpSettings.DEFAULT_ID)
                .orElseThrow(() -> new IllegalStateException("SMTP settings have not been configured yet."));
        if (!settings.isEnabled() || !StringUtils.hasText(settings.getHost())
                || !StringUtils.hasText(settings.getFromAddress())) {
            throw new IllegalStateException("Configure and enable SMTP and the sender address before testing.");
        }
        return settings;
    }

    private JavaMailSenderImpl buildSender(SmtpSettings settings) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(settings.getHost());
        sender.setPort(settings.getPort());
        sender.setUsername(settings.getUsername());
        sender.setPassword(cryptoService.decrypt(settings.getPasswordEncrypted()));
        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", Boolean.toString(settings.isAuth()));
        properties.put("mail.smtp.starttls.enable", Boolean.toString(settings.isStartTlsEnabled()));
        properties.put("mail.smtp.ssl.enable", Boolean.toString(settings.isSslEnabled()));
        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "5000");
        properties.put("mail.smtp.writetimeout", "5000");
        return sender;
    }
}