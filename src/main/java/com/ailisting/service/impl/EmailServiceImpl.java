package com.ailisting.service.impl;

import com.ailisting.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@ailisting.com}")
    private String fromEmail;

    @Value("${app.email.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "AI Listing - Password Reset Request";
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        String htmlContent = buildPasswordResetHtml(resetUrl);

        sendHtmlEmail(to, subject, htmlContent);
        log.info("Password reset email sent to: {}", to);
    }

    @Override
    @Async
    public void sendEmailVerification(String to, String verificationToken) {
        String subject = "AI Listing - Verify Your Email";
        String verifyUrl = frontendUrl + "/verify-email?token=" + verificationToken;
        String htmlContent = buildEmailVerificationHtml(verifyUrl);

        sendHtmlEmail(to, subject, htmlContent);
        log.info("Email verification sent to: {}", to);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        String subject = "Welcome to AI Listing!";
        String htmlContent = buildWelcomeHtml(fullName);

        sendHtmlEmail(to, subject, htmlContent);
        log.info("Welcome email sent to: {}", to);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildPasswordResetHtml(String resetUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #f8f9fa; padding: 30px; border-radius: 10px;">
                        <h2 style="color: #333; text-align: center;">Password Reset Request</h2>
                        <p style="color: #555; font-size: 16px;">You requested a password reset for your AI Listing account.</p>
                        <p style="color: #555; font-size: 16px;">Click the button below to reset your password:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #4CAF50; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-size: 16px;">Reset Password</a>
                        </div>
                        <p style="color: #999; font-size: 14px;">This link will expire in 1 hour.</p>
                        <p style="color: #999; font-size: 14px;">If you didn't request this, please ignore this email.</p>
                    </div>
                </body>
                </html>
                """.formatted(resetUrl);
    }

    private String buildEmailVerificationHtml(String verifyUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #f8f9fa; padding: 30px; border-radius: 10px;">
                        <h2 style="color: #333; text-align: center;">Verify Your Email</h2>
                        <p style="color: #555; font-size: 16px;">Thank you for registering with AI Listing!</p>
                        <p style="color: #555; font-size: 16px;">Please verify your email address by clicking the button below:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #2196F3; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-size: 16px;">Verify Email</a>
                        </div>
                        <p style="color: #999; font-size: 14px;">If you didn't create an account, please ignore this email.</p>
                    </div>
                </body>
                </html>
                """.formatted(verifyUrl);
    }

    private String buildWelcomeHtml(String fullName) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #f8f9fa; padding: 30px; border-radius: 10px;">
                        <h2 style="color: #333; text-align: center;">Welcome to AI Listing!</h2>
                        <p style="color: #555; font-size: 16px;">Hi %s,</p>
                        <p style="color: #555; font-size: 16px;">Your account has been created successfully. You can now start generating AI-powered product listings for your e-commerce business.</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s/dashboard" style="background-color: #4CAF50; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-size: 16px;">Go to Dashboard</a>
                        </div>
                        <p style="color: #999; font-size: 14px;">Features you can explore:</p>
                        <ul style="color: #555; font-size: 14px;">
                            <li>AI-powered listing generation</li>
                            <li>Multi-platform support (Amazon, Flipkart, Meesho, Shopify)</li>
                            <li>SEO optimization</li>
                            <li>Bulk listing management</li>
                        </ul>
                    </div>
                </body>
                </html>
                """.formatted(fullName, frontendUrl);
    }
}