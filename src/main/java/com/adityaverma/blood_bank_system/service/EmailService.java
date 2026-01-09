package com.adityaverma.blood_bank_system.service;

import com.adityaverma.blood_bank_system.model.User;
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
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Async
    public void sendVerificationEmail(User user) {
        String subject = "Verify Your Email - Blood Bank System";
        String message = String.format(
                "Dear %s,\n\n" +
                        "Please click the following link to verify your email address:\n" +
                        "%s/verify-email?token=%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "Thank you,\n" +
                        "Blood Bank Team",
                user.getFullName(),
                "http://localhost:3000", // Should be from config
                user.getEmailVerificationToken()
        );

        sendEmail(user.getEmail(), subject, message);
    }

    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        String subject = "Password Reset Request - Blood Bank System";
        String message = String.format(
                "Dear %s,\n\n" +
                        "You have requested to reset your password. Please click the following link:\n" +
                        "%s/reset-password?token=%s\n\n" +
                        "This link will expire in 30 minutes.\n\n" +
                        "If you didn't request this, please ignore this email.\n\n" +
                        "Thank you,\n" +
                        "Blood Bank Team",
                user.getFullName(),
                "http://localhost:3000", // Should be from config
                resetToken
        );

        sendEmail(user.getEmail(), subject, message);
    }

    @Async
    public void sendDonationReminder(User user) {
        String subject = "You're Eligible to Donate Blood Again!";
        String message = String.format(
                "Dear %s,\n\n" +
                        "You are now eligible to donate blood again. Your donation can save up to 3 lives.\n\n" +
                        "Please schedule your donation appointment at your convenience.\n\n" +
                        "Thank you for being a life saver!\n\n" +
                        "Blood Bank Team",
                user.getFullName()
        );

        sendEmail(user.getEmail(), subject, message);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}