package com.adityaverma.blood_bank_system.model;

public enum BloodGroup {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-");

    private final String displayName;

    BloodGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BloodGroup fromString(String value) {
        if (value == null) return null;
        return switch (value.toUpperCase().replace(" ", "")) {
            case "A+", "APOSITIVE" -> A_POSITIVE;
            case "A-", "ANEGATIVE" -> A_NEGATIVE;
            case "B+", "BPOSITIVE" -> B_POSITIVE;
            case "B-", "BNEGATIVE" -> B_NEGATIVE;
            case "AB+", "ABPOSITIVE" -> AB_POSITIVE;
            case "AB-", "ABNEGATIVE" -> AB_NEGATIVE;
            case "O+", "OPOSITIVE" -> O_POSITIVE;
            case "O-", "ONEGATIVE" -> O_NEGATIVE;
            default -> throw new IllegalArgumentException("Invalid blood group: " + value);
        };
    }
}