package com.zetapilot.manager.repository;

import com.zetapilot.manager.model.SmtpSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmtpSettingsRepository extends JpaRepository<SmtpSettings, Short> {
}