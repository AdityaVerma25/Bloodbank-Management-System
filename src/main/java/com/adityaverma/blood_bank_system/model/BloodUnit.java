package com.adityaverma.blood_bank_system.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "blood_units")
@CompoundIndex(name = "status_expiry_idx", def = "{'status': 1, 'expiry_date': 1}")
@CompoundIndex(name = "bloodbank_status_idx", def = "{'blood_bank_id': 1, 'status': 1}")
public class BloodUnit {
    @Id
    private String id;

    @Indexed(unique = true)
    @Field("unit_id")
    private String unitId;

    @Field("donation_id")
    private String donationId;

    @Field("donor_id")
    private String donorId;

    @Field("blood_group")
    private BloodGroup bloodGroup;

    @Field("component_type")
    private BloodComponent componentType;

    @Field("volume_ml")
    private Integer volumeMl;

    @Field("collection_date")
    private LocalDate collectionDate;

    @Field("expiry_date")
    private LocalDate expiryDate;

    @Field("storage_location")
    private String storageLocation;

    @Field("storage_temperature")
    private String storageTemperature;

    @Field("test_results")
    private TestResults testResults;

    @Builder.Default
    private Status status = Status.AVAILABLE;

    @Field("blood_bank_id")
    private String bloodBankId;

    @Field("current_location")
    private String currentLocation;

    @Field("reserved_for")
    private String reservedFor;

    @Field("reserved_until")
    private LocalDateTime reservedUntil;

    @Field("issued_to")
    private String issuedTo;

    @Field("issued_date")
    private LocalDateTime issuedDate;

    @Field("discarded_reason")
    private String discardedReason;

    @Field("discarded_by")
    private String discardedBy;

    @Field("discarded_date")
    private LocalDateTime discardedDate;

    @Field("qr_code_data")
    private String qrCodeData;

    @Field("batch_number")
    private String batchNumber;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    // Java 21 Record for Test Results
    public record TestResults(
            @Field("hiv") boolean hiv,
            @Field("hepatitis_b") boolean hepatitisB,
            @Field("hepatitis_c") boolean hepatitisC,
            @Field("syphilis") boolean syphilis,
            @Field("malaria") boolean malaria,
            @Field("other_tests") String otherTests,
            @Field("tested_by") String testedBy,
            @Field("test_date") LocalDate testDate,
            @Field("lab_id") String labId,
            @Field("certificate_number") String certificateNumber
    ) {
        public boolean isAllTestsPassed() {
            return !hiv && !hepatitisB && !hepatitisC && !syphilis && !malaria;
        }
    }

    public enum Status {
        AVAILABLE("Available"),
        RESERVED("Reserved"),
        ISSUED("Issued"),
        IN_TRANSIT("In Transit"),
        TRANSFERRED("Transferred"),
        DISCARDED("Discarded"),
        EXPIRED("Expired");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean isExpiringSoon(int daysThreshold) {
        LocalDate warningDate = LocalDate.now().plusDays(daysThreshold);
        return !expiryDate.isAfter(warningDate) && !expiryDate.isBefore(LocalDate.now());
    }

    public boolean canBeReserved() {
        return status == Status.AVAILABLE && !isExpired();
    }
}