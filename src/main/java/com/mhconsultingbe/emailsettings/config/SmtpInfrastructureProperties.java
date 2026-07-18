package com.mhconsultingbe.emailsettings.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.mail.smtp")
public class SmtpInfrastructureProperties {
    private String host = "";
    private int port = 587;
    private boolean auth = true;
    private boolean startTls = true;
    private boolean implicitSsl;
    private int connectionTimeout = 10000;
    private int readTimeout = 10000;
    private int writeTimeout = 10000;
    private String environmentUsername = "";
    private String environmentPassword = "";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public boolean isStartTls() {
        return startTls;
    }

    public void setStartTls(boolean startTls) {
        this.startTls = startTls;
    }

    public boolean isImplicitSsl() {
        return implicitSsl;
    }

    public void setImplicitSsl(boolean implicitSsl) {
        this.implicitSsl = implicitSsl;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public String getEnvironmentUsername() {
        return environmentUsername;
    }

    public void setEnvironmentUsername(String environmentUsername) {
        this.environmentUsername = environmentUsername;
    }

    public String getEnvironmentPassword() {
        return environmentPassword;
    }

    public void setEnvironmentPassword(String environmentPassword) {
        this.environmentPassword = environmentPassword;
    }
}
