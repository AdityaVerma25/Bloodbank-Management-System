package com.adityaverma.blood_bank_system.repository;

import com.adityaverma.blood_bank_system.model.BloodUnit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloodUnitRepository extends MongoRepository<BloodUnit, String> {

    Optional<BloodUnit> findByUnitId(String unitId);

    @Query("{'bloodGroup': ?0, 'componentType': ?1, 'status': 'AVAILABLE', 'expiryDate': {$gte: ?2}}")
    List<BloodUnit> findAvailableByBloodGroupAndComponent(String bloodGroup, String componentType, LocalDate today);

    @Query("{'bloodBankId': ?0, 'status': ?1, 'expiryDate': {$gte: ?2}}")
    List<BloodUnit> findByBloodBankAndStatus(String bloodBankId, BloodUnit.Status status, LocalDate today);

    @Query("{'expiryDate': {$gte: ?0, $lte: ?1}, 'status': 'AVAILABLE'}")
    List<BloodUnit> findExpiringBetween(LocalDate start, LocalDate end);

    @Query("{'expiryDate': {$lt: ?0}, 'status': 'AVAILABLE'}")
    List<BloodUnit> findExpiredUnits(LocalDate today);

    @Query("{'donorId': ?0}")
    List<BloodUnit> findByDonorId(String donorId);

    @Query("{'bloodBankId': ?0}")
    List<BloodUnit> findByBloodBankId(String bloodBankId);

    @Query(value = "{'bloodBankId': ?0, 'status': ?1}", count = true)
    long countByBloodBankIdAndStatus(String bloodBankId, BloodUnit.Status status);

    @Query("{'status': 'RESERVED', 'reservedUntil': {$lt: ?0}}")
    List<BloodUnit> findExpiredReservations(LocalDateTime now);

    @Query(value = "{'status': 'AVAILABLE', 'expiryDate': {$gte: ?0}}", count = true)
    long countAvailableUnits(LocalDate today);
}