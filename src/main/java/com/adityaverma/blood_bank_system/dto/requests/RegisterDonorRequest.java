package com.adityaverma.blood_bank_system.dto.requests;

import com.adityaverma.blood_bank_system.model.BloodGroup;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegisterDonorRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
        String phone,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotNull(message = "Blood group is required")
        BloodGroup bloodGroup,

        @NotBlank(message = "RH factor is required")
        @Pattern(regexp = "^[+-]$", message = "RH factor must be + or -")
        String rhFactor,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotBlank(message = "Gender is required")
        @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
        String gender,

        @NotNull(message = "Weight is required")
        @Min(value = 40, message = "Weight must be at least 40 kg")
        @Max(value = 200, message = "Weight cannot exceed 200 kg")
        Double weightKg,

        @NotNull(message = "Height is required")
        @Min(value = 100, message = "Height must be at least 100 cm")
        @Max(value = 250, message = "Height cannot exceed 250 cm")
        Double heightCm,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "State is required")
        String state,

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "Postal code is required")
        String postalCode,

        boolean acceptTerms
) {}