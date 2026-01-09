package com.adityaverma.blood_bank_system.dto.responses;

import java.time.LocalDate;
import java.util.Map;

public record BloodInventorySummaryResponse(
        String bloodBankId,
        String bloodBankName,
        long totalAvailable,
        long totalReserved,
        long totalIssued,
        long totalExpiringSoon,
        long totalDiscarded,
        Map<String, Long> byBloodGroup,
        Map<String, Long> byComponent,
        LocalDate lastUpdated,
        LocalDate nextExpiryDate,
        boolean isStockLow
) {
    public BloodInventorySummaryResponse {
        if (totalAvailable < 0) {
            throw new IllegalArgumentException("Total available cannot be negative");
        }
        isStockLow = totalAvailable < 50;
    }
}