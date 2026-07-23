package com.ailisting.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String resetToken);
    void sendEmailVerification(String to, String verificationToken);
    void sendWelcomeEmail(String to, String fullName);
}