package com.adityaverma.blood_bank_system.repository;

import com.adityaverma.blood_bank_system.model.BloodRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloodRequestRepository extends MongoRepository<BloodRequest, String> {

    Optional<BloodRequest> findByRequestId(String requestId);

    @Query("{'hospitalId': ?0}")
    List<BloodRequest> findByHospitalId(String hospitalId);

    @Query("{'status': ?0}")
    List<BloodRequest> findByStatus(BloodRequest.RequestStatus status);

    @Query("{'urgencyLevel': {$in: ['CRITICAL', 'URGENT']}, 'status': {$in: ['PENDING', 'APPROVED']}}")
    List<BloodRequest> findEmergencyRequests();

    @Query("{'createdAt': {$gte: ?0, $lte: ?1}}")
    List<BloodRequest> findByDateRange(LocalDateTime start, LocalDateTime end);

    @Query("{'patientId': ?0}")
    List<BloodRequest> findByPatientId(String patientId);

    @Query("{'allocatedUnits': {$in: [?0]}}")
    List<BloodRequest> findRequestsByAllocatedUnit(String unitId);
}