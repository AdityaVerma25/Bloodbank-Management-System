package com.adityaverma.blood_bank_system.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "donations")
public class Donation {
    @Id
    private String id;

    @Field("donation_id")
    private String donationId;

    @Field("donor_id")
    private String donorId;

    @Field("blood_bank_id")
    private String bloodBankId;

    @Field("donation_date")
    private LocalDateTime donationDate;

    @Field("donation_type")
    private DonationType donationType;

    @Field("blood_group")
    private BloodGroup bloodGroup;

    @Field("volume_ml")
    private Integer volumeMl;

    @Field("hemoglobin_level")
    private Double hemoglobinLevel;

    @Field("blood_pressure")
    private String bloodPressure;

    @Field("pulse_rate")
    private Integer pulseRate;

    @Field("temperature")
    private Double temperature;

    @Field("weight_kg")
    private Double weightKg;

    @Field("medical_screening")
    private MedicalScreening medicalScreening;

    @Field("generated_units")
    private List<GeneratedUnit> generatedUnits;

    @Field("donation_status")
    @Builder.Default
    private DonationStatus donationStatus = DonationStatus.COMPLETED;

    @Field("rejection_reason")
    private String rejectionReason;

    @Field("staff_id")
    private String staffId;

    @Field("notes")
    private String notes;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    public record MedicalScreening(
            @Field("has_fever") boolean hasFever,
            @Field("has_cold") boolean hasCold,
            @Field("has_tattoo") boolean hasTattoo,
            @Field("tattoo_date") LocalDateTime tattooDate,
            @Field("has_piercing") boolean hasPiercing,
            @Field("piercing_date") LocalDateTime piercingDate,
            @Field("has_surgery") boolean hasSurgery,
            @Field("surgery_date") LocalDateTime surgeryDate,
            @Field("has_medication") boolean hasMedication,
            @Field("medication_details") String medicationDetails,
            @Field("has_travel") boolean hasTravel,
            @Field("travel_details") String travelDetails,
            @Field("has_sexual_risk") boolean hasSexualRisk,
            @Field("sexual_risk_details") String sexualRiskDetails,
            @Field("screening_notes") String screeningNotes,
            @Field("screened_by") String screenedBy
    ) {
        public boolean isEligible() {
            if (hasFever || hasCold) return false;
            if (hasTattoo && tattooDate != null
                    && tattooDate.isAfter(LocalDateTime.now().minusMonths(6))) return false;
            if (hasPiercing && piercingDate != null
                    && piercingDate.isAfter(LocalDateTime.now().minusMonths(6))) return false;
            if (hasSurgery && surgeryDate != null
                    && surgeryDate.isAfter(LocalDateTime.now().minusMonths(6))) return false;
            return !hasSexualRisk;
        }
    }

    public record GeneratedUnit(
            @Field("unit_id") String unitId,
            @Field("component_type") BloodComponent componentType,
            @Field("volume_ml") Integer volumeMl,
            @Field("storage_location") String storageLocation
    ) {}

    public enum DonationType {
        WHOLE_BLOOD("Whole Blood"),
        PLASMA("Plasma"),
        PLATELETS("Platelets"),
        DOUBLE_RED_CELLS("Double Red Cells");

        private final String displayName;

        DonationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DonationStatus {
        SCHEDULED("Scheduled"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        REJECTED("Rejected"),
        CANCELLED("Cancelled"),
        DEFERRED("Deferred");

        private final String displayName;

        DonationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}