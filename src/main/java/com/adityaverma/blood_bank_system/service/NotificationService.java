package com.adityaverma.blood_bank_system.service;

import com.adityaverma.blood_bank_system.model.BloodRequest;
import com.adityaverma.blood_bank_system.model.BloodUnit;
import com.adityaverma.blood_bank_system.model.Donation;
import com.adityaverma.blood_bank_system.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final JavaMailSender mailSender;
    private final EmailService emailService;

    @Async
    public void sendExpiryAlert(List<BloodUnit> units) {
        if (units.isEmpty()) return;

        String bloodBankId = units.get(0).getBloodBankId();
        String subject = "Blood Units Expiring Soon - Alert";
        String message = String.format(
                "%d blood units are expiring within 3 days. Please review inventory.",
                units.size()
        );

        sendEmail("bloodbank@example.com", subject, message);
    }

    @Async
    public void notifyEmergencyRequest(BloodRequest request) {
        String subject = "URGENT: Blood Request - " + request.getPatientName();
        String message = String.format(
                "Emergency blood request:\n" +
                        "Patient: %s\n" +
                        "Blood Group: %s\n" +
                        "Required: %d units of %s\n" +
                        "Urgency: %s\n" +
                        "Hospital: %s",
                request.getPatientName(),
                request.getBloodGroup(),
                request.getQuantityUnits(),
                request.getComponentType(),
                request.getUrgencyLevel(),
                request.getHospitalId()
        );

        sendEmail("emergency@bloodbank.com", subject, message);
    }

    @Async
    public void sendDonationReminder(User donor) {
        emailService.sendDonationReminder(donor);
    }

    @Async
    public void sendThankYouEmail(User donor, Donation donation) {
        String subject = "Thank You for Your Donation!";
        String message = String.format(
                "Dear %s,\n\n" +
                        "Thank you for donating blood. Your donation can save up to 3 lives.\n\n" +
                        "Donation Details:\n" +
                        "Date: %s\n" +
                        "Type: %s\n" +
                        "Volume: %d ml\n\n" +
                        "You will be eligible to donate again after 3 months.\n\n" +
                        "Thank you for being a life saver!\n\n" +
                        "Blood Bank Team",
                donor.getFullName(),
                donation.getDonationDate(),
                donation.getDonationType(),
                donation.getVolumeMl()
        );

        sendEmail(donor.getEmail(), subject, message);
    }

    @Async
    public void notifyRequestAllocation(BloodRequest request) {
        String subject = "Blood Request Allocated - " + request.getRequestId();
        String message = String.format(
                "Blood request %s has been allocated.\n" +
                        "Patient: %s\n" +
                        "Allocated Units: %d",
                request.getRequestId(),
                request.getPatientName(),
                request.getAllocatedUnits() != null ? request.getAllocatedUnits().size() : 0
        );

        sendEmail("hospital@example.com", subject, message);
    }

    @Async
    public void notifyRequestDispatch(BloodRequest request) {
        String subject = "Blood Request Dispatched - " + request.getRequestId();
        String message = String.format(
                "Blood request %s has been dispatched to hospital.\n" +
                        "Patient: %s\n" +
                        "Expected delivery: ASAP",
                request.getRequestId(),
                request.getPatientName()
        );

        sendEmail("hospital@example.com", subject, message);
    }

    @Async
    public void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

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