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
@Document(collection = "blood_requests")
public class BloodRequest {
    @Id
    private String id;

    @Field("request_id")
    private String requestId;

    @Field("hospital_id")
    private String hospitalId;

    @Field("patient_id")
    private String patientId;

    @Field("patient_name")
    private String patientName;

    @Field("patient_age")
    private Integer patientAge;

    @Field("patient_gender")
    private String patientGender;

    @Field("blood_group")
    private BloodGroup bloodGroup;

    @Field("component_type")
    private BloodComponent componentType;

    @Field("quantity_units")
    private Integer quantityUnits;

    @Field("required_by")
    private LocalDateTime requiredBy;

    @Field("reason")
    private String reason;

    @Field("urgency_level")
    private UrgencyLevel urgencyLevel;

    @Field("doctor_name")
    private String doctorName;

    @Field("doctor_contact")
    private String doctorContact;

    @Field("requested_by")
    private String requestedBy;

    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Field("allocated_units")
    private List<String> allocatedUnits;

    @Field("rejection_reason")
    private String rejectionReason;

    @Field("completed_at")
    private LocalDateTime completedAt;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    public enum UrgencyLevel {
        CRITICAL("Critical - Immediate"),
        URGENT("Urgent - Within 2 hours"),
        HIGH("High - Within 6 hours"),
        NORMAL("Normal - Within 24 hours"),
        SCHEDULED("Scheduled - Planned");

        private final String displayName;

        UrgencyLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RequestStatus {
        PENDING("Pending"),
        APPROVED("Approved"),
        ALLOCATED("Allocated"),
        DISPATCHED("Dispatched"),
        DELIVERED("Delivered"),
        REJECTED("Rejected"),
        CANCELLED("Cancelled");

        private final String displayName;

        RequestStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}