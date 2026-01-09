package com.adityaverma.blood_bank_system.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User implements UserDetails {
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String phone;

    private String password;

    @Field("full_name")
    private String fullName;

    @Field("blood_group")
    private BloodGroup bloodGroup;

    @Field("rh_factor")
    private String rhFactor;

    private String address;
    private String city;
    private String state;
    private String country;

    @Field("postal_code")
    private String postalCode;

    @Field("date_of_birth")
    private LocalDate dateOfBirth;

    private String gender;

    @Field("weight_kg")
    private Double weightKg;

    @Field("height_cm")
    private Double heightCm;

    @Builder.Default
    @Field("is_donor")
    private boolean isDonor = false;

    @Builder.Default
    @Field("is_patient")
    private boolean isPatient = false;

    @Builder.Default
    @Field("is_hospital_staff")
    private boolean isHospitalStaff = false;

    @Builder.Default
    @Field("is_blood_bank_staff")
    private boolean isBloodBankStaff = false;

    @Builder.Default
    @Field("is_admin")
    private boolean isAdmin = false;

    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Field("last_login_at")
    private LocalDateTime lastLoginAt;

    @Field("last_donation_date")
    private LocalDate lastDonationDate;

    @Builder.Default
    @Field("is_active")
    private boolean isActive = true;

    @Builder.Default
    @Field("is_locked")
    private boolean isLocked = false;

    @Field("failed_login_attempts")
    @Builder.Default
    private int failedLoginAttempts = 0;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Field("email_verified")
    private boolean emailVerified = false;

    @Builder.Default
    @Field("phone_verified")
    private boolean phoneVerified = false;

    @Field("email_verification_token")
    private String emailVerificationToken;

    @Field("email_verification_expires")
    private LocalDateTime emailVerificationExpires;

    @Field("password_reset_token")
    private String passwordResetToken;

    @Field("password_reset_expires")
    private LocalDateTime passwordResetExpires;

    @Field("profile_image_url")
    private String profileImageUrl;

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    // Business methods
    public boolean isEligibleForDonation() {
        if (!isDonor || !isActive) return false;

        if (lastDonationDate == null) return true;

        LocalDate nextEligibleDate = lastDonationDate.plusMonths(3);
        return LocalDate.now().isAfter(nextEligibleDate)
                && weightKg != null && weightKg >= 50.0
                && dateOfBirth != null
                && dateOfBirth.isBefore(LocalDate.now().minusYears(18))
                && dateOfBirth.isAfter(LocalDate.now().minusYears(65));
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.isLocked = true;
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.isLocked = false;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Role {
    private String name;
    @Builder.Default
    private Set<String> permissions = new HashSet<>();
}