package com.zetapilot.manager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tab_smtp_settings")
public class SmtpSettings {

    public static final short DEFAULT_ID = 1;

    @Id
    private Short id;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, length = 255)
    private String host;

    @Column(nullable = false)
    private int port;

    @Column(nullable = false, length = 255)
    private String username;

    @Column(name = "password_encrypted", nullable = false, columnDefinition = "TEXT")
    private String passwordEncrypted;

    @Column(nullable = false)
    private boolean auth;

    @Column(name = "starttls_enabled", nullable = false)
    private boolean startTlsEnabled;

    @Column(name = "ssl_enabled", nullable = false)
    private boolean sslEnabled;

    @Column(name = "from_address", nullable = false, length = 255)
    private String fromAddress;

    @Column(name = "from_name", nullable = false, length = 150)
    private String fromName;

    protected SmtpSettings() {
    }

    public static SmtpSettings defaults() {
        SmtpSettings settings = new SmtpSettings();
        settings.id = DEFAULT_ID;
        settings.enabled = false;
        settings.host = "";
        settings.port = 587;
        settings.username = "";
        settings.passwordEncrypted = "";
        settings.auth = true;
        settings.startTlsEnabled = true;
        settings.sslEnabled = false;
        settings.fromAddress = "";
        settings.fromName = "ZetaPilotAI";
        return settings;
    }

    public Short getId() { return id; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordEncrypted() { return passwordEncrypted; }
    public void setPasswordEncrypted(String passwordEncrypted) { this.passwordEncrypted = passwordEncrypted; }
    public boolean isAuth() { return auth; }
    public void setAuth(boolean auth) { this.auth = auth; }
    public boolean isStartTlsEnabled() { return startTlsEnabled; }
    public void setStartTlsEnabled(boolean startTlsEnabled) { this.startTlsEnabled = startTlsEnabled; }
    public boolean isSslEnabled() { return sslEnabled; }
    public void setSslEnabled(boolean sslEnabled) { this.sslEnabled = sslEnabled; }
    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }
}