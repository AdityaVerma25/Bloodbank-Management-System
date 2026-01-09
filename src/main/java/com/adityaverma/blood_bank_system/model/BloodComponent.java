package com.adityaverma.blood_bank_system.model;

public enum BloodComponent {
    WHOLE_BLOOD("Whole Blood", 35, "1-6°C"),
    PLASMA("Plasma", 365, "-25°C or below"),
    PLATELETS("Platelets", 5, "20-24°C"),
    RED_BLOOD_CELLS("Red Blood Cells", 42, "1-6°C"),
    CRYOPRECIPITATE("Cryoprecipitate", 365, "-25°C or below");

    private final String displayName;
    private final int shelfLifeDays;
    private final String storageTemperature;

    BloodComponent(String displayName, int shelfLifeDays, String storageTemperature) {
        this.displayName = displayName;
        this.shelfLifeDays = shelfLifeDays;
        this.storageTemperature = storageTemperature;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getShelfLifeDays() {
        return shelfLifeDays;
    }

    public String getStorageTemperature() {
        return storageTemperature;
    }
}