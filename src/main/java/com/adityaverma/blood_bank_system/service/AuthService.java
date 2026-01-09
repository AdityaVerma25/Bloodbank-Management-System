package com.adityaverma.blood_bank_system.service;

import com.adityaverma.blood_bank_system.dto.requests.RegisterDonorRequest;
import com.adityaverma.blood_bank_system.model.User;
import com.adityaverma.blood_bank_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public User registerDonor(RegisterDonorRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email is already registered");
        }

        if (userRepository.existsByPhone(request.phone())) {
            throw new RuntimeException("Phone number is already registered");
        }

        User user = User.builder()
                .email(request.email())
                .phone(request.phone())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .bloodGroup(request.bloodGroup())
                .rhFactor(request.rhFactor())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .weightKg(request.weightKg())
                .heightCm(request.heightCm())
                .address(request.address())
                .city(request.city())
                .state(request.state())
                .country(request.country())
                .postalCode(request.postalCode())
                .isDonor(true)
                .isActive(true)
                .build();

        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationExpires(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);

        emailService.sendVerificationEmail(savedUser);

        log.info("New donor registered: {} ({})", savedUser.getFullName(), savedUser.getEmail());

        return savedUser;
    }

    @Transactional
    public boolean verifyEmail(String token) {
        var user = userRepository.findByEmailVerificationToken(token, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpires(null);

        userRepository.save(user);

        log.info("Email verified for user: {}", user.getEmail());
        return true;
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusMinutes(30));

        userRepository.save(user);

        emailService.sendPasswordResetEmail(user, resetToken);

        log.info("Password reset initiated for user: {}", user.getEmail());
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        var user = userRepository.findByPasswordResetToken(token, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        user.setFailedLoginAttempts(0);
        user.setLocked(false);

        userRepository.save(user);

        log.info("Password reset successful for user: {}", user.getEmail());
        return true;
    }
}