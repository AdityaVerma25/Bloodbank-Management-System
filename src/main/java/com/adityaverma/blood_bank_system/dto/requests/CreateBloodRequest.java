package com.adityaverma.blood_bank_system.dto.requests;

import com.adityaverma.blood_bank_system.model.BloodComponent;
import com.adityaverma.blood_bank_system.model.BloodGroup;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateBloodRequest(
        @NotBlank String patientName,
        @NotNull BloodGroup bloodGroup,
        @NotNull BloodComponent componentType,
        @Min(1) Integer quantityUnits,
        @NotBlank String urgencyLevel,
        @NotBlank String hospitalId,
        String reason,
        String doctorName,
        LocalDateTime requiredBy
) {}