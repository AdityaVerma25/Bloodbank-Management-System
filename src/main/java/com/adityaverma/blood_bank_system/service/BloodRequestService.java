package com.adityaverma.blood_bank_system.service;

import com.adityaverma.blood_bank_system.dto.requests.CreateBloodRequest;
import com.adityaverma.blood_bank_system.model.BloodRequest;
import com.adityaverma.blood_bank_system.model.BloodUnit;
import com.adityaverma.blood_bank_system.repository.BloodRequestRepository;
import com.adityaverma.blood_bank_system.repository.BloodUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloodRequestService {
    private final BloodRequestRepository bloodRequestRepository;
    private final BloodUnitRepository bloodUnitRepository;
    private final NotificationService notificationService;
    private final IdGenerator idGenerator;

    @Transactional
    public BloodRequest createRequest(CreateBloodRequest request) {
        BloodRequest bloodRequest = BloodRequest.builder()
                .requestId(idGenerator.generateRequestId())
                .patientName(request.patientName())
                .bloodGroup(request.bloodGroup())
                .componentType(request.componentType())
                .quantityUnits(request.quantityUnits())
                .urgencyLevel(BloodRequest.UrgencyLevel.valueOf(request.urgencyLevel()))
                .hospitalId(request.hospitalId())
                .reason(request.reason())
                .doctorName(request.doctorName())
                .requiredBy(request.requiredBy())
                .status(BloodRequest.RequestStatus.PENDING)
                .build();

        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        if (request.urgencyLevel().equals("CRITICAL")) {
            notificationService.notifyEmergencyRequest(savedRequest);
        }

        log.info("Created blood request {} for patient {}",
                savedRequest.getRequestId(), savedRequest.getPatientName());

        return savedRequest;
    }

    @Transactional
    public BloodRequest allocateUnits(String requestId, List<String> unitIds) {
        BloodRequest request = bloodRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        List<String> allocated = new ArrayList<>();

        for (String unitId : unitIds) {
            BloodUnit unit = bloodUnitRepository.findByUnitId(unitId)
                    .orElseThrow(() -> new RuntimeException("Unit not found: " + unitId));

            if (unit.getStatus() == BloodUnit.Status.AVAILABLE) {
                unit.setStatus(BloodUnit.Status.RESERVED);
                unit.setReservedFor(requestId);
                unit.setReservedUntil(LocalDateTime.now().plusHours(2));
                bloodUnitRepository.save(unit);
                allocated.add(unitId);
            }
        }

        request.setAllocatedUnits(allocated);
        request.setStatus(BloodRequest.RequestStatus.ALLOCATED);
        BloodRequest updated = bloodRequestRepository.save(request);

        notificationService.notifyRequestAllocation(updated);

        return updated;
    }

    @Transactional
    public BloodRequest issueRequest(String requestId) {
        BloodRequest request = bloodRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        if (request.getStatus() != BloodRequest.RequestStatus.ALLOCATED) {
            throw new RuntimeException("Request must be allocated before issuing");
        }

        if (request.getAllocatedUnits() != null) {
            for (String unitId : request.getAllocatedUnits()) {
                BloodUnit unit = bloodUnitRepository.findByUnitId(unitId)
                        .orElseThrow(() -> new RuntimeException("Unit not found: " + unitId));

                unit.setStatus(BloodUnit.Status.ISSUED);
                unit.setIssuedTo(request.getHospitalId());
                unit.setIssuedDate(LocalDateTime.now());
                bloodUnitRepository.save(unit);
            }
        }

        request.setStatus(BloodRequest.RequestStatus.DISPATCHED);
        BloodRequest updated = bloodRequestRepository.save(request);

        notificationService.notifyRequestDispatch(updated);

        return updated;
    }

    public List<BloodRequest> getEmergencyRequests() {
        return bloodRequestRepository.findEmergencyRequests();
    }

    public List<BloodRequest> getHospitalRequests(String hospitalId) {
        return bloodRequestRepository.findByHospitalId(hospitalId);
    }

    public BloodRequest getRequest(String requestId) {
        return bloodRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));
    }
}