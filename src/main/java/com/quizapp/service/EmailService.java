package com.quizapp.service;

import com.quizapp.entity.SystemSettingEntity;
import com.quizapp.repository.SystemSettingRepository;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final SystemSettingRepository systemSettingRepository;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String defaultHost;

    @Value("${spring.mail.port:587}")
    private int defaultPort;

    @Value("${spring.mail.username:}")
    private String defaultUsername;

    @Value("${spring.mail.password:}")
    private String defaultPassword;

    private JavaMailSenderImpl dynamicMailSender;
    private String activeFromEmail;
    private String activeHost;
    private int activePort = 587;
    private boolean isConfigured = false;

    @Autowired
    public EmailService(SystemSettingRepository systemSettingRepository) {
        this.systemSettingRepository = systemSettingRepository;
    }

    @PostConstruct
    public synchronized void init() {
        reloadConfigFromDatabase();
    }

    public synchronized void reloadConfigFromDatabase() {
        String host = getSetting("mail.host", defaultHost);
        int port = 587;
        try {
            port = Integer.parseInt(getSetting("mail.port", String.valueOf(defaultPort)));
        } catch (NumberFormatException ignored) {}

        String username = getSetting("mail.username", defaultUsername);
        String password = getSetting("mail.password", defaultPassword);

        this.activeHost = host;
        this.activePort = port;
        this.activeFromEmail = username;

        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(host != null && !host.isBlank() ? host.trim() : "smtp.gmail.com");
            sender.setPort(port > 0 ? port : 587);
            sender.setUsername(username.trim());
            sender.setPassword(password.trim());

            Properties props = sender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.connectiontimeout", "6000");
            props.put("mail.smtp.timeout", "6000");
            props.put("mail.smtp.writetimeout", "6000");

            this.dynamicMailSender = sender;
            this.isConfigured = true;
            log.info("Initialized dynamic JavaMailSender from MySQL database for: {}", username);
        } else {
            this.dynamicMailSender = null;
            this.isConfigured = false;
            log.info("Email service initialized in DEV/CONSOLE mode (no SMTP credentials in MySQL).");
        }
    }

    public synchronized Map<String, Object> saveEmailConfig(String host, int port, String username, String password) {
        saveSetting("mail.host", host != null && !host.isBlank() ? host.trim() : "smtp.gmail.com");
        saveSetting("mail.port", String.valueOf(port > 0 ? port : 587));
        
        if (username != null) {
            saveSetting("mail.username", username.trim());
        }
        
        if (password != null && !password.isBlank()) {
            saveSetting("mail.password", password.trim());
        }

        reloadConfigFromDatabase();

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "SMTP Email configuration successfully saved to MySQL database!");
        res.put("configured", this.isConfigured);
        res.put("host", this.activeHost);
        res.put("port", this.activePort);
        res.put("username", this.activeFromEmail);
        return res;
    }

    public Map<String, Object> getEmailConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("host", this.activeHost != null ? this.activeHost : "smtp.gmail.com");
        map.put("port", this.activePort > 0 ? this.activePort : 587);
        map.put("username", this.activeFromEmail != null ? this.activeFromEmail : "");
        map.put("hasPassword", getSetting("mail.password", defaultPassword) != null && !getSetting("mail.password", defaultPassword).isBlank());
        map.put("configured", this.isConfigured);
        return map;
    }

    public Map<String, Object> testEmailConnection(String toEmail) {
        Map<String, Object> res = new HashMap<>();
        if (!isConfigured || dynamicMailSender == null) {
            res.put("success", false);
            res.put("message", "SMTP is not yet configured with username and password in MySQL database.");
            return res;
        }

        try {
            MimeMessage message = dynamicMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(activeFromEmail, "Oakridge International Academy");
            helper.setTo(toEmail);
            helper.setSubject("Oakridge Portal - SMTP Email Connection Successful");

            String html = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px; max-width: 500px;'>"
                    + "<h2 style='color: #059669; margin-top: 0;'>✅ SMTP Email Connection Test Passed!</h2>"
                    + "<p style='color: #334155; line-height: 1.5;'>This test confirmation email proves that your Spring Boot backend successfully authenticated with your SMTP provider via credentials stored in MySQL.</p>"
                    + "<hr style='border: none; border-top: 1px solid #f1f5f9; margin: 15px 0;'/>"
                    + "<p style='color: #64748b; font-size: 13px;'>Host: " + activeHost + ":" + activePort + "<br/>Sender: " + activeFromEmail + "</p>"
                    + "</div>";

            helper.setText(html, true);
            dynamicMailSender.send(message);

            res.put("success", true);
            res.put("message", "Test email successfully sent to " + toEmail + "! Please check your inbox.");
            log.info("Test email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Test email connection failed: {}", e.getMessage());
            res.put("success", false);
            res.put("message", "SMTP Error: " + e.getMessage() + " (Check email/password, or ensure 16-character Google App Password is used).");
        }
        return res;
    }

    public boolean sendOtpEmail(String toEmail, String recipientName, String otpCode) {
        boolean sentViaSmtp = false;

        if (dynamicMailSender != null && activeFromEmail != null && !activeFromEmail.isBlank()) {
            try {
                MimeMessage message = dynamicMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(activeFromEmail, "Oakridge International Academy");
                helper.setTo(toEmail);
                helper.setSubject("Oakridge Portal - Your Password Reset Verification Code: " + otpCode);

                String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;'>"
                        + "<div style='text-align: center; margin-bottom: 24px;'>"
                        + "<h2 style='color: #4f46e5; margin: 0;'>Oakridge International Academy</h2>"
                        + "<p style='color: #64748b; font-size: 14px; margin-top: 4px;'>Institutional Single-Sign-On Security Service</p>"
                        + "</div>"
                        + "<p style='font-size: 16px; color: #1e293b;'>Hello <strong>" + recipientName + "</strong>,</p>"
                        + "<p style='font-size: 14px; color: #475569; line-height: 1.6;'>We received a request to reset the password for your institutional account associated with <strong>" + toEmail + "</strong>. Use the 6-digit verification code below to complete your password update:</p>"
                        + "<div style='text-align: center; margin: 28px 0;'>"
                        + "<span style='display: inline-block; font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #4f46e5; background: #eef2ff; border: 2px dashed #818cf8; padding: 12px 28px; border-radius: 8px; font-family: monospace;'>"
                        + otpCode + "</span>"
                        + "</div>"
                        + "<p style='font-size: 13px; color: #d97706; background: #fffbeb; border: 1px solid #fde68a; padding: 10px 14px; border-radius: 6px; line-height: 1.4;'>"
                        + "⏳ <strong>Security Notice:</strong> This verification code will expire in <strong>5 minutes</strong>. If you did not initiate this request, please contact your School Administrator immediately."
                        + "</p>"
                        + "<hr style='border: none; border-top: 1px solid #f1f5f9; margin: 24px 0;' />"
                        + "<p style='font-size: 12px; color: #94a3b8; text-align: center;'>Oakridge International Academy • Automated Examination & Student Records Portal</p>"
                        + "</div>";

                helper.setText(htmlContent, true);
                dynamicMailSender.send(message);
                sentViaSmtp = true;
                log.info("Successfully dispatched OTP email via dynamic SMTP to: {}", toEmail);
            } catch (Exception e) {
                log.warn("SMTP email dispatch to {} failed: {}. Falling back to server console logging.", toEmail, e.getMessage());
            }
        }

        // Always log to backend console
        log.info("================================================================================");
        log.info("[BACKEND EMAIL DISPATCH] Recipient: {} ({}) | Status: {}", recipientName, toEmail, (sentViaSmtp ? "SENT VIA SMTP" : "DEV/CONSOLE FALLBACK"));
        log.info("[SECURITY OTP CODE] >>> {} <<< (Valid for 5 minutes)", otpCode);
        log.info("================================================================================");

        return sentViaSmtp;
    }

    private String getSetting(String key, String fallback) {
        Optional<SystemSettingEntity> opt = systemSettingRepository.findById(key);
        if (opt.isPresent() && opt.get().getValue() != null && !opt.get().getValue().isBlank()) {
            return opt.get().getValue();
        }
        return fallback;
    }

    private void saveSetting(String key, String val) {
        systemSettingRepository.save(new SystemSettingEntity(key, val));
    }
}
