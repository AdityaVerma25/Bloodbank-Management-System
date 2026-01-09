package com.adityaverma.blood_bank_system.service;

import com.adityaverma.blood_bank_system.dto.responses.BloodInventorySummaryResponse;
import com.adityaverma.blood_bank_system.model.BloodUnit;
import com.adityaverma.blood_bank_system.repository.BloodUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloodInventoryService {
    private final BloodUnitRepository bloodUnitRepository;
    private final NotificationService notificationService;

    private static final int EXPIRY_WARNING_DAYS = 3;

    @Cacheable(value = "bloodInventory", key = "#bloodBankId")
    public BloodInventorySummaryResponse getInventorySummary(String bloodBankId) {
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(EXPIRY_WARNING_DAYS);

        List<BloodUnit> allUnits = bloodUnitRepository.findByBloodBankId(bloodBankId);

        Map<BloodUnit.Status, List<BloodUnit>> unitsByStatus = allUnits.stream()
                .collect(Collectors.groupingBy(BloodUnit::getStatus));

        List<BloodUnit> availableUnits = unitsByStatus.getOrDefault(BloodUnit.Status.AVAILABLE, List.of())
                .stream()
                .filter(unit -> !unit.isExpired())
                .toList();

        List<BloodUnit> expiringSoon = availableUnits.stream()
                .filter(unit -> unit.isExpiringSoon(EXPIRY_WARNING_DAYS))
                .toList();

        Map<String, Long> groupCount = availableUnits.stream()
                .collect(Collectors.groupingBy(
                        unit -> unit.getBloodGroup().getDisplayName(),
                        Collectors.counting()
                ));

        Map<String, Long> componentCount = availableUnits.stream()
                .collect(Collectors.groupingBy(
                        unit -> unit.getComponentType().getDisplayName(),
                        Collectors.counting()
                ));

        long totalReserved = unitsByStatus.getOrDefault(BloodUnit.Status.RESERVED, List.of()).size();
        long totalIssued = unitsByStatus.getOrDefault(BloodUnit.Status.ISSUED, List.of()).size();
        long totalDiscarded = unitsByStatus.getOrDefault(BloodUnit.Status.DISCARDED, List.of()).size();

        LocalDate nextExpiryDate = availableUnits.stream()
                .map(BloodUnit::getExpiryDate)
                .min(LocalDate::compareTo)
                .orElse(null);

        boolean isStockLow = availableUnits.size() < 50;

        return new BloodInventorySummaryResponse(
                bloodBankId,
                "Blood Bank " + bloodBankId, // This should come from BloodBankService
                availableUnits.size(),
                totalReserved,
                totalIssued,
                expiringSoon.size(),
                totalDiscarded,
                groupCount,
                componentCount,
                today,
                nextExpiryDate,
                isStockLow
        );
    }

    public List<BloodUnit> searchAvailableUnits(SearchCriteria criteria) {
        LocalDate today = LocalDate.now();

        return switch (criteria) {
            case SearchCriteria.ByBloodGroup byBloodGroup ->
                    bloodUnitRepository.findAvailableByBloodGroupAndComponent(
                            byBloodGroup.bloodGroup(),
                            null,
                            today
                    );
            case SearchCriteria.ByComponent byComponent ->
                    bloodUnitRepository.findAvailableByBloodGroupAndComponent(
                            null,
                            byComponent.componentType(),
                            today
                    );
            case SearchCriteria.ByLocation byLocation ->
                    bloodUnitRepository.findByBloodBankAndStatus(
                            byLocation.location(),
                            BloodUnit.Status.AVAILABLE,
                            today
                    );
            case SearchCriteria.Combined combined -> {
                var units = bloodUnitRepository.findAvailableByBloodGroupAndComponent(
                        combined.bloodGroup(),
                        combined.componentType(),
                        today
                );
                yield units.stream()
                        .filter(unit -> unit.getBloodBankId().equals(combined.location()))
                        .toList();
            }
        };
    }

    @Transactional
    public BloodUnit reserveUnit(String unitId, String requestId) {
        BloodUnit unit = bloodUnitRepository.findByUnitId(unitId)
                .orElseThrow(() -> new RuntimeException("Blood unit not found: " + unitId));

        if (!unit.canBeReserved()) {
            throw new RuntimeException("Blood unit cannot be reserved: " + unitId);
        }

        unit.setStatus(BloodUnit.Status.RESERVED);
        unit.setReservedFor(requestId);
        unit.setReservedUntil(LocalDateTime.now().plusHours(2));
        unit.setUpdatedAt(LocalDateTime.now());

        BloodUnit saved = bloodUnitRepository.save(unit);
        clearInventoryCache(saved.getBloodBankId());

        log.info("Blood unit {} reserved for request {}", unitId, requestId);
        return saved;
    }

    @Transactional
    public BloodUnit issueUnit(String unitId, String hospitalId) {
        BloodUnit unit = bloodUnitRepository.findByUnitId(unitId)
                .orElseThrow(() -> new RuntimeException("Blood unit not found: " + unitId));

        if (unit.getStatus() != BloodUnit.Status.RESERVED) {
            throw new RuntimeException("Blood unit must be reserved before issuing");
        }

        unit.setStatus(BloodUnit.Status.ISSUED);
        unit.setIssuedTo(hospitalId);
        unit.setIssuedDate(LocalDateTime.now());
        unit.setUpdatedAt(LocalDateTime.now());

        BloodUnit saved = bloodUnitRepository.save(unit);
        clearInventoryCache(saved.getBloodBankId());

        log.info("Blood unit {} issued to hospital {}", unitId, hospitalId);
        return saved;
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void checkExpiringUnits() {
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(EXPIRY_WARNING_DAYS);

        List<BloodUnit> expiringUnits = bloodUnitRepository.findExpiringBetween(today, warningDate);

        if (!expiringUnits.isEmpty()) {
            log.warn("Found {} units expiring within {} days", expiringUnits.size(), EXPIRY_WARNING_DAYS);

            expiringUnits.stream()
                    .collect(Collectors.groupingBy(BloodUnit::getBloodBankId))
                    .forEach((bloodBankId, units) -> {
                        notificationService.sendExpiryAlert(units);
                        log.info("Sent expiry alert for {} units in blood bank {}", units.size(), bloodBankId);
                    });
        }
    }

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void updateExpiredUnits() {
        LocalDate today = LocalDate.now();
        List<BloodUnit> expiredUnits = bloodUnitRepository.findExpiredUnits(today);

        expiredUnits.forEach(unit -> {
            unit.setStatus(BloodUnit.Status.EXPIRED);
            unit.setDiscardedReason("Auto-expired");
            unit.setDiscardedBy("System");
            unit.setDiscardedDate(LocalDateTime.now());
            unit.setUpdatedAt(LocalDateTime.now());
        });

        if (!expiredUnits.isEmpty()) {
            bloodUnitRepository.saveAll(expiredUnits);
            log.info("Updated {} expired units to EXPIRED status", expiredUnits.size());
        }
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<BloodUnit> expiredReservations = bloodUnitRepository.findExpiredReservations(now);

        expiredReservations.forEach(unit -> {
            unit.setStatus(BloodUnit.Status.AVAILABLE);
            unit.setReservedFor(null);
            unit.setReservedUntil(null);
            unit.setUpdatedAt(now);
        });

        if (!expiredReservations.isEmpty()) {
            bloodUnitRepository.saveAll(expiredReservations);
            log.info("Released {} expired reservations", expiredReservations.size());
        }
    }

    @CacheEvict(value = "bloodInventory", key = "#bloodBankId")
    public void clearInventoryCache(String bloodBankId) {
        log.debug("Cleared inventory cache for blood bank: {}", bloodBankId);
    }

    @CacheEvict(value = "bloodInventory", allEntries = true)
    public void clearAllInventoryCache() {
        log.info("Cleared all blood inventory cache");
    }

    public sealed interface SearchCriteria {
        record ByBloodGroup(String bloodGroup) implements SearchCriteria {}
        record ByComponent(String componentType) implements SearchCriteria {}
        record ByLocation(String location) implements SearchCriteria {}
        record Combined(String bloodGroup, String componentType, String location) implements SearchCriteria {}
    }
}